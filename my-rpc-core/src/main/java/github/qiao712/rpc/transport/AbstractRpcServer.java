package github.qiao712.rpc.transport;

import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.proto.SerializationType;

public abstract class AbstractRpcServer implements RpcServer{
    protected RequestHandler requestHandler;
    protected SerializationType serializationType;

    //连接最长空闲时间(ms)。空闲时间超出后断开。为0标识不断开。
    protected int maxIdleTime = 0;

    public AbstractRpcServer(RequestHandler requestHandler, SerializationType serializationType) {
        this.requestHandler = requestHandler;
        this.serializationType = serializationType;
    }

    @Override
    public void setSerializationType(SerializationType serializationType) {
        this.serializationType = serializationType;
    }

    @Override
    public SerializationType getSerializationType() {
        return serializationType;
    }

    @Override
    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

    @Override
    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    @Override
    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }
}
