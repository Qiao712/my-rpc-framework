package github.qiao712.rpc.transport;

import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.proto.SerializationType;

public abstract class AbstractRpcServer implements RpcServer{
    protected RequestHandler requestHandler;
    protected SerializationType serializationType;

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
}
