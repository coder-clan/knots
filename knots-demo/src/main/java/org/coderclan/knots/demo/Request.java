package org.coderclan.knots.demo;

import java.io.Serializable;

public class Request<T> implements Serializable {
    private T data;
    private String requestId;

    public Request(T data, String requestId) {
        this.data = data;
        this.requestId = requestId;
    }

    public T getData() {
        return data;
    }

    public String getRequestId() {
        return requestId;
    }
}
