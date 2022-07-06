package github.qiao712;

import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.handler.SimpleRequestHandler;
import github.qiao712.rpc.registry.ServiceProvider;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceRegistry;
import github.qiao712.rpc.transport.RpcServer;
import github.qiao712.rpc.transport.netty.server.NettyRpcServer;
import github.qiao712.service.TestServiceImpl;

import java.net.InetSocketAddress;

public class TestNettyProvider {
    public static void main(String[] args) {
        InetSocketAddress providerAddress = new InetSocketAddress("127.0.0.1", 9712);
        InetSocketAddress zkAddress = new InetSocketAddress("8.141.151.176", 2181);

        ServiceRegistry serviceRegistry = new ZookeeperServiceRegistry(providerAddress, zkAddress);

        ServiceProvider serviceProvider = new ServiceProvider(serviceRegistry);

        RequestHandler requestHandler = new SimpleRequestHandler(serviceProvider);

        serviceProvider.addService(new TestServiceImpl());

        RpcServer rpcServer = new NettyRpcServer(9712, requestHandler);
        rpcServer.setMaxIdleTime(3000);

        rpcServer.start();
    }
}
