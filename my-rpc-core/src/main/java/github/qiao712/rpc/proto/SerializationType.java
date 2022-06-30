package github.qiao712.rpc.proto;

import github.qiao712.rpc.serializer.HessianSerializer;
import github.qiao712.rpc.serializer.Serializer;
import github.qiao712.rpc.serializer.JDKSerializer;

/**
 * 序列化方式
 */
public enum SerializationType{
    JDK_SERIALIZATION(new JDKSerializer()),
    HESSIAN_SERIALIZATION(new HessianSerializer());

    private final Serializer serializer;

    SerializationType(Serializer serializer) {
        this.serializer = serializer;
    }

    public Serializer getSerializer(){
        return serializer;
    }
}
