package github.qiao712.serializer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 序列化器
 */
public interface Serializer {
    /**
     * 序列化对象
     */
    byte[] serialize(Object obj);

    /**
     * 序列化对象
     */
    void serialize(Object obj, OutputStream outputStream);

    /**
     * 反序列化
     */
    <T> T deserialize(byte[] data, Class<T> cls);

    /**
     * 反序列化
     */
    <T> T deserialize(InputStream inputStream, Class<T> cls);
}
