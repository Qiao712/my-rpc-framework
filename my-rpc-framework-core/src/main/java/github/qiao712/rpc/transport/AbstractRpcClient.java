package github.qiao712.rpc.transport;

import github.qiao712.rpc.proto.SerializationType;

public abstract class AbstractRpcClient implements RpcClient {
    protected SerializationType serializationType = SerializationType.JDK_SERIALIZATION;        //所使用的序列化方式
    protected long responseTimeout = 0;         //响应的超时时间(ms). 为0表示一直等待.
    protected long heartbeatInterval = 60000;   //心跳间隔时间(ms). 为0表示不发送心跳.

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
        if(responseTimeout < 0){
            throw new IllegalArgumentException("responseTimeout < 0.");
        }
        this.responseTimeout = responseTimeout;
    }

    @Override
    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    @Override
    public void setHeartbeatInterval(long heartbeatInterval) {
        if(heartbeatInterval < 0){
            throw new IllegalArgumentException("heartbeatInterval < 0.");
        }
        this.heartbeatInterval = heartbeatInterval;
    }
}
