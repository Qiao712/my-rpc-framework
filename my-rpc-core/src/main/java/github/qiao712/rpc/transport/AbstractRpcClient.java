package github.qiao712.rpc.transport;

import github.qiao712.rpc.proto.SerializationType;
import io.netty.util.internal.ObjectUtil;

public abstract class AbstractRpcClient implements RpcClient {
    protected SerializationType serializationType = SerializationType.JDK_SERIALIZATION;        //所使用的序列化方式
    protected long responseTimeout = 0;      //响应的超时时间(ms). 为0表示一直等待.

    @Override
    public SerializationType getSerializationType() {
        return serializationType;
    }

    @Override
    public void setSerializationType(SerializationType serializationType) {
        this.serializationType = serializationType;
    }

    @Override
    public long getResponseTimeout() {
        return responseTimeout;
    }

    @Override
    public void setResponseTimeout(long responseTimeout) {
        ObjectUtil.checkPositiveOrZero(responseTimeout, "响应等待超时时间");
        this.responseTimeout = responseTimeout;
    }
}
