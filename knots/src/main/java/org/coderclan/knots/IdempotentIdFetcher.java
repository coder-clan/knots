package org.coderclan.knots;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Idempotent ID Fetcher. Idempotent ID may be within Method Parameters, may be transferred as HTTP Header, etc.
 * It used by {@link IdempotentAspect} to get Idempotent ID.
 *
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
public interface IdempotentIdFetcher {
    /**
     * Get Idempotent ID
     *
     * @param proceedingJoinPoint Information of Invoking method, can get method arguments from it.
     * @return Idempotent Id
     */
    String getIdempotentId(ProceedingJoinPoint proceedingJoinPoint);
}
