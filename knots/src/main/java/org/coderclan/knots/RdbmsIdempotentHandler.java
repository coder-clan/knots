package org.coderclan.knots;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.Objects;

/**
 * Idempotent Handler which use Relational Database to persistent data(lock and invocation result).
 *
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
public class RdbmsIdempotentHandler implements IdempotentHandler, ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log = LoggerFactory.getLogger(RdbmsIdempotentHandler.class);

    private final DataSource dataSource;
    private final Serializer serializer;
    private final String tableName;

    private final String selectSql;
    private final String updateSql;
    @Autowired
    private KnotsProperties properties;


    public RdbmsIdempotentHandler(Serializer serializer, DataSource dataSource, String tableName) {
        this.serializer = serializer;
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.selectSql = "select idempotent_id,method_result,success from " + tableName + " where idempotent_id=? ";
        this.updateSql = "update  " + tableName + "  set method_result=?, success=? where idempotent_id=? ";
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent applicationEvent) {
        // create table when starting.
        createTable();
    }

    @Override
    public Object lockOrReturnPreviousResult(String idempotentId, ProceedingJoinPoint joinPoint) throws Exception {
        for (int tried = 0; tried < properties.getRetries(); tried++) {
            ResultSet rs = null;
            try (
                    Connection connection = dataSource.getConnection();
                    PreparedStatement ps = connection.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)
            ) {
                ps.setString(1, idempotentId);
                rs = ps.executeQuery();

                if (rs.next()) { // Invocation of the same idempotentId has been started.

                    boolean failed = rs.getBoolean("success"); // success

                    // previous invocation has been finished. but failed to invoke.
                    if (failed) {

                        // we need to reset the following column before retrying invocation.
                        rs.updateBinaryStream("method_result", null);
                        rs.updateBoolean("success", false);
                        commit(connection);

                        log.info("Previous invocation failed, retry. idempotentId={}", idempotentId);
                        return null;
                    }

                    // previous invocation has been finished. and succeeded.
                    Object previousResult = getResult(rs);
                    if (Objects.nonNull(previousResult)) {
                        log.debug("Previous invocation succeeded. idempotentId={}", idempotentId);
                        return previousResult;
                    }

                    // not result, and not marked as failed, the previous invocation has not been finished yet, wait
                    log.info("Found previous invoke but it has NOT returned yet! idempotentId={}, tried={}", idempotentId, tried);
                    Thread.sleep(properties.getRetryWait());
                    continue;

                } else { // Invocation of the same idempotentId has NOT been started.
                    log.trace("First invocation of idempotentId={}.", idempotentId);
                    rs.moveToInsertRow();
                    rs.updateString("idempotent_id", idempotentId);

                    try {
                        rs.insertRow();
                        commit(connection);
                    } catch (SQLException e) {
                        // SQLException may be caused by bugs, or Primary Key violating.
                        //
                        // if PK is violated, it must be because of the same impotent ID has been started to process.
                        // continue the tried to get the result.
                        log.info("", e);
                        continue;
                    }
                    return null;
                }


            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
        log.error("Found previous invoke but previous invoke is timeout! idempotentId={}", idempotentId);
        throw new RuntimeException("Found previous invoke but previous invoke is timeout!");
    }

    private void commit(Connection connection) throws SQLException {
        if (!connection.getAutoCommit()) {
            connection.commit();
        }
    }

    private Object getResult(ResultSet rs) throws Exception {
        try (InputStream stream = rs.getBinaryStream("method_result")) {
            return serializer.deserialize(stream);
        }
    }

    @Override
    public void saveResult(String idempotentId, Object result, boolean success, ProceedingJoinPoint joinPoint) throws Exception {
        final byte[] resultByte = Objects.isNull(result) ? null : serializer.serialize(result);
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(this.updateSql)
        ) {
            //set method_result=?, success=? where idempotent_id=?
            ps.setBytes(1, resultByte); // method_result
            ps.setBoolean(2, success); // success
            ps.setString(3, idempotentId); // idempotent_id
            int count = ps.executeUpdate();
            commit(connection);

//            if(true){
//                throw new RuntimeException("eee");
//            }

            // "count" should be ONE
            if (count != 1) {
                log.error("Save return result failed. idempotentId={}", idempotentId);
            }
        }
    }

    private void createTable() {
        log.info("Idempotent log table name: {}", this.tableName);
        try (
                Connection conn = dataSource.getConnection();
                Statement statement = conn.createStatement()
        ) {

            statement.execute("\n" +
                    "CREATE TABLE IF NOT EXISTS " + this.tableName + " (\n" +
                    "  idempotent_id varchar(64) NOT NULL,\n" +
                    "  success tinyint NOT NULL DEFAULT '0',\n" +
                    "  create_time datetime NOT NULL default CURRENT_TIMESTAMP(),\n" +
                    "  method_result varbinary(10240),\n" +
                    "  PRIMARY KEY (idempotent_id)\n" +
                    ")");
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
