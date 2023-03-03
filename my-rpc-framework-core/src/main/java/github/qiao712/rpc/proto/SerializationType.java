package github.qiao712.rpc.proto;

import github.qiao712.rpc.serializer.HessianSerializer;
import github.qiao712.rpc.serializer.JSONSerializer;
import github.qiao712.rpc.serializer.Serializer;
import github.qiao712.rpc.serializer.JDKSerializer;

/**
 * 序列化方式
 */
public enum SerializationType{
    NONE(null),
    JDK_SERIALIZATION(new JDKSerializer()),
    HESSIAN_SERIALIZATION(new HessianSerializer()),
    JSON_SERIALIZATION(new JSONSerializer());

    private final Serializer serializer;

    SerializationType(Serializer serializer) {
        this.serializer = serializer;
    }

    public Serializer getSerializer(){
        return serializer;
    }
}
