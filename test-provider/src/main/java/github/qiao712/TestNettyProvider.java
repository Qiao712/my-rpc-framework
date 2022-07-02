package github.qiao712;

import github.qiao712.rpc.handler.DefaultRequestHandler;
import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.registry.SimpleServiceRegistry;
import github.qiao712.rpc.transport.RpcServer;
import github.qiao712.rpc.transport.netty.server.NettyRpcServer;
import github.qiao712.service.TestServiceImpl;

public class TestNettyProvider {
    public static void main(String[] args) {
        ServiceRegistry serviceRegistry = new SimpleServiceRegistry();
        serviceRegistry.register("testService", new TestServiceImpl());
        RequestHandler requestHandler = new DefaultRequestHandler(serviceRegistry);
        RpcServer rpcServer = new NettyRpcServer(9712, requestHandler);
        rpcServer.start();
    }
}
