package org.coderclan.knots.demo;

import java.io.Serializable;

public class Result<T> implements Serializable {
    private Boolean success;
    private T data;

    public Result(Boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }
}
