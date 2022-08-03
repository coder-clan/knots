package org.coderclan.knots;

import java.io.InputStream;

/**
 * Serializer used to serialize or deserialize the return value of the Invocation.
 *
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
public interface Serializer {
    Object deserialize(InputStream is) throws Exception;

    byte[] serialize(Object obj) throws Exception;
}
