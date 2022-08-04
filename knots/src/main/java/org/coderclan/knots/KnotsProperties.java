package org.coderclan.knots;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration Properties of the Knots.
 *
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
@ConfigurationProperties(prefix = "org.coderclan.knots")
public class KnotsProperties {
    /**
     * Times of retrying to get previous invocation result. Default: 50.
     */
    private int retries = 50;
    /**
     * Pause (unit: millisecond) to wait between retrying. Default: 200ms.
     */
    private int retryWait = 200;

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getRetryWait() {
        return retryWait;
    }

    public void setRetryWait(int retryWait) {
        this.retryWait = retryWait;
    }
}
