package github.qiao712.rpc.transport;

import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.proto.SerializationType;

public interface RpcServer {
    void start();

    RequestHandler getRequestHandler();

    void setRequestHandler(RequestHandler requestHandler);

    void setSerializationType(SerializationType serializationType);

    SerializationType getSerializationType();

    int getMaxIdleTime();

    void setMaxIdleTime(int maxIdleTime);
}
