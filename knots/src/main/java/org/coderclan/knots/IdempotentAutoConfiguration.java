package org.coderclan.knots;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
@Configuration
@EnableConfigurationProperties(KnotsProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class IdempotentAutoConfiguration {

    @ConditionalOnMissingBean(IdempotentHandler.class)
    @ConditionalOnBean(DataSource.class)
    @Bean
    IdempotentHandler idempotentValidator(Serializer serializer, DataSource ds, @Value("${org.coderclan.knots.table:sys_idempotent_log}") String tableName) {
        return new RdbmsIdempotentHandler(serializer, ds, tableName);
    }

    @ConditionalOnMissingBean(IdempotentAspect.class)
    @Bean
    IdempotentAspect idempotentAspect() {
        return new IdempotentAspect();
    }

    @ConditionalOnMissingBean(IdempotentIdFetcher.class)
    @Bean
    IdempotentIdFetcher idempotentIdFetcher() {
        return new ArgumentIdempotentIdFetcher();
    }


    @ConditionalOnMissingBean(Serializer.class)
    @Bean
    Serializer serializer() {
        return new JdkSerializer();
    }
}
