package org.coderclan.knots.demo;

import org.coderclan.knots.ResultChecker;
import org.springframework.stereotype.Component;

@Component
public class MyResultChecker implements ResultChecker {
    @Override
    public boolean isSuccess(Object result) {
        if (result instanceof Result) {
            return ((Result) result).getSuccess();
        }
        throw new IllegalArgumentException("Only support type: " + Result.class);
    }
}
