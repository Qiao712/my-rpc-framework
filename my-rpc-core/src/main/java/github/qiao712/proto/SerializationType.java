package github.qiao712.proto;

import github.qiao712.serializer.JDKSerializer;
import github.qiao712.serializer.Serializer;

/**
 * 序列化方式
 */
public enum SerializationType{
    JDK_SERIALIZATION(new JDKSerializer());

    private final Serializer serializer;

    SerializationType(Serializer serializer) {
        this.serializer = serializer;
    }

    public Serializer getSerializer(){
        return serializer;
    }
}
