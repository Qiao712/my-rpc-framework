package github.qiao712;

import github.qiao712.rpc.proto.SerializationType;
import github.qiao712.rpc.handler.DefaultRequestHandler;
import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.registry.SimpleServiceRegistry;
import github.qiao712.rpc.transport.bio.server.BIORpcServer;
import github.qiao712.service.TestServiceImpl;

public class TestProvider {
    public static void main(String[] args) {
        ServiceRegistry serviceRegistry = new SimpleServiceRegistry();
        serviceRegistry.register("testService", new TestServiceImpl());
        RequestHandler requestHandler = new DefaultRequestHandler(serviceRegistry);
        BIORpcServer rpcServer = new BIORpcServer(9712, requestHandler);
        rpcServer.setSerializationType(SerializationType.HESSIAN_SERIALIZATION);

        rpcServer.start();
    }
}
