package org.coderclan.knots;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Idempotent Handler, is used to obtain a lock of the IdempotentId, to get result of previous invocation of the same IdempotentId,
 * to save the result. (The following invocation of the same IdempotentId will return the saved result without executing the invocation directly)
 * <strong>Important: The result should be saved to the datasource which business logic of the invocation using. Use the the same Transaction of the invocation, don't create new transaction. </strong>
 *
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
public interface IdempotentHandler {
    /**
     * Try to obtain a lock of the IdempotentId to prevent other threads to invoke the method.
     * If locked successfully, return null.
     * If failed to obtain the Lock, it means other thread has started to execute, wait other thread to finish the execution and return the result.
     *
     * @param idempotentId Idempotent ID
     * @param joinPoint
     * @return null if lock successfully(the invocation of the same IdempotentId has NOT started to execute), The return value of the invocation if other thread executed it.
     * @throws Exception throws when waiting for other thread finishing the execution timeout.
     */
    Object lockOrReturnPreviousResult(String idempotentId, ProceedingJoinPoint joinPoint) throws Exception;


    /**
     * Save the information(idempotentId, return value, and success state) of the invocation.
     * Make sure the information is saved to the same datasource of business logics, to let the information rollback within the transaction of business logic.
     *
     * @param idempotentId
     * @param result       return value of the invocation
     * @param success      true if success invoked
     * @param joinPoint
     */
    void saveResult(String idempotentId, Object result, boolean success, ProceedingJoinPoint joinPoint) throws Exception;
}
