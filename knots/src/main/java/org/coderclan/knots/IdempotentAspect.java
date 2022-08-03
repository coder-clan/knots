package org.coderclan.knots;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.coderclan.knots.annotation.Idempotent;
import org.coderclan.knots.annotation.IdempotentIdExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import java.util.Objects;

/**
 * Intercept invocation of Method to make sure invocations with the same Idempotent ID only execute once.
 * The method match following conditions will be intercepted:
 * <ul>
 *     <li>Method is annotated by {@link Idempotent} directly.</li>
 *     <li>Method is annotated by any annotation which is annotated by {@link Idempotent}. e.g. {@link IdempotentIdExpression}</li>
 *     <li>Any method is in a class which is annotated by {@link Idempotent} directly. </li>
 *     <li>Any method is in a class which is Annotated by any annotation which is annotated by {@link Idempotent}. </li>
 *     <li>Any method is in a class whose parent class is annotated by {@link Idempotent} directly. </li>
 * </ul>
 *
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
@Aspect
public class IdempotentAspect implements Ordered {

    private static Logger logger = LoggerFactory.getLogger(IdempotentAspect.class);

    @Autowired
    private IdempotentIdFetcher idempotentIdFetcher;
    @Autowired
    private IdempotentHandler idempotentHandler;
    @Autowired
    private ResultChecker resultChecker;
    private final int aspectOrder;

    public IdempotentAspect() {
        this.aspectOrder = Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * @param aspectOrder the Order of aspects. it should be lower than the order of Aspect for @Transactional,
     *                    since if {@link IdempotentHandler#saveResult(String, Object, boolean, ProceedingJoinPoint)} failed, the database transaction should be rollback.
     */
    public IdempotentAspect(int aspectOrder) {
        this.aspectOrder = aspectOrder;
    }

    @Around("execution(@org.coderclan.knots.annotation.Idempotent * *(..))" + // Methods annotated by @Idempotent
            " || execution(@(@org.coderclan.knots.annotation.Idempotent *) * *(..))" + // Methods annotated by any annotation which is annotated by @Idempotent
            " || within(@org.coderclan.knots.annotation.Idempotent *)" +  // Classes annotated by @Idempotent
            " || within(@(@org.coderclan.knots.annotation.Idempotent *) *)"  // Classes annotated by any annotation which is annotated by @Idempotent
    )
    public Object aroundService(ProceedingJoinPoint joinPoint) {
        String idempotentId = idempotentIdFetcher.getIdempotentId(joinPoint);
        if (Objects.isNull(idempotentId)) {
            throw new IllegalStateException("Idempotent ID is null, method=" + joinPoint.getSignature().toLongString());
        }

        Object result;
        try {
            result = idempotentHandler.lockOrReturnPreviousResult(idempotentId, joinPoint);
        } catch (Exception e) {
            logger.error("Exception countered while checking result. idempotentId={}", e);
            throw new RuntimeException(e);
        }

        if (Objects.isNull(result)) {
            // result is null, the invocation is not executed, execute the invocation.
            boolean fail = false;
            try {
                result = joinPoint.proceed();
                fail = !(this.resultChecker.isSuccess(result));
            } catch (Throwable e) {
                fail = true;
                logger.info("Exception countered while invoking idempotentId={}", idempotentId);
                throw new RuntimeException(e);
            } finally {
                try {
                    idempotentHandler.saveResult(idempotentId, result, fail, joinPoint);
                } catch (Exception e) {
                    // IdempotentAspect should be wrapped by @Transactional
                    // if save result failed, the Database Transaction will be rollback.
                    // All database writes including the write made by org.coderclan.knots.IdempotentHandler.saveResult will be rollback
                    throw new RuntimeException("Exception countered while save result. idempotentId=" + idempotentId, e);
                }
            }
        } else {
            logger.info("Repeat invoke found for idempotentId={}, return directly!", idempotentId);
        }
        return result;


    }

    private Object proceed(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public int getOrder() {
        return this.aspectOrder;
    }
}
