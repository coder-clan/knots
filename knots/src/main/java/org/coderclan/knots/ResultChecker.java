package org.coderclan.knots;

/**
 * Check whether if the invocation was successfully executed. check org.coderclan.knots.demo.MyResultChecker in knots-demo
 *
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
public interface ResultChecker {
    boolean isSuccess(Object result);
}
