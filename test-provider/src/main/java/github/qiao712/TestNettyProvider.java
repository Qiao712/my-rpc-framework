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
import java.util.Random;

public class TestNettyProvider {
    public static void main(String[] args) {
        Random random = new Random(System.nanoTime());
        int port = 9712 + random.nextInt(1000);
        System.out.println("port: " + port);
        InetSocketAddress providerAddress = new InetSocketAddress("127.0.0.1", port);
        InetSocketAddress zkAddress = new InetSocketAddress("8.141.151.176", 2181);

        ServiceRegistry serviceRegistry = new ZookeeperServiceRegistry(providerAddress, zkAddress);

        ServiceProvider serviceProvider = new ServiceProvider(serviceRegistry);

        RequestHandler requestHandler = new SimpleRequestHandler(serviceProvider);

        serviceProvider.addService(new TestServiceImpl());

        RpcServer rpcServer = new NettyRpcServer(port, requestHandler);
        rpcServer.setMaxIdleTime(3000);

        rpcServer.start();
    }
}
