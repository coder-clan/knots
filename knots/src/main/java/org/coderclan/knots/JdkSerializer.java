package org.coderclan.knots;

import java.io.*;
import java.util.Objects;

/**
 * JDK Serializer, use {@link ObjectOutputStream} and {@link ObjectInputStream} to serialize and deserialize.
 * Return type of the invocation should implement {@link Serializable}
 *
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
public class JdkSerializer implements Serializer {
    @Override
    public Object deserialize(InputStream stream) throws IOException, ClassNotFoundException {
        if (Objects.isNull(stream)) {
            return null;
        }
        try (ObjectInputStream os = new ObjectInputStream(stream)) {
            return os.readObject();
        }
    }

    @Override
    public byte[] serialize(Object obj) throws IOException {
        if (Objects.isNull(obj)) {
            return null;
        }
        try (
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(os);
        ) {
            out.writeObject(obj);
            return os.toByteArray();
        }
    }
}
