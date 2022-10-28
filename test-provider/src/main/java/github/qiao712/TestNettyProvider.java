package github.qiao712;

import github.qiao712.rpc.handler.ConcurrentRequestHandler;
import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.handler.SerializableRequestHandler;
import github.qiao712.rpc.handler.SimpleRequestHandler;
import github.qiao712.rpc.registry.ServiceProvider;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceRegistry;
import github.qiao712.rpc.transport.RpcServer;
import github.qiao712.rpc.transport.netty.server.NettyRpcServer;
import github.qiao712.service.TestServiceImpl;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestNettyProvider {
    public static void main(String[] args) {
        Random random = new Random(System.nanoTime());
        int port = 9712 + random.nextInt(1000);
        System.out.println("port: " + port);
        InetSocketAddress providerAddress = new InetSocketAddress("127.0.0.1", port);
        InetSocketAddress zkAddress = new InetSocketAddress("114.116.245.83", 2181);

        //服务注册组件
        ServiceRegistry serviceRegistry = new ZookeeperServiceRegistry(providerAddress, zkAddress);

        //服务的容器，用于注册服务对象并通过服务注册组件注册
        ServiceProvider serviceProvider = new ServiceProvider(serviceRegistry);

        //RpcRequest处理器
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
//                Runtime.getRuntime().availableProcessors() * 2,
//                10, TimeUnit.SECONDS,
//                new ArrayBlockingQueue<>(1000),
//                new ThreadPoolExecutor.CallerRunsPolicy());
//        RequestHandler requestHandler = new ConcurrentRequestHandler(serviceProvider, threadPoolExecutor);
//        RequestHandler requestHandler = new SimpleRequestHandler(serviceProvider);
        RequestHandler requestHandler = new SerializableRequestHandler(serviceProvider);

        //注册服务
        serviceProvider.addService(new TestServiceImpl());

        //创建RpcServer，接收并处理Rpc调用请求
        RpcServer rpcServer = new NettyRpcServer(port, requestHandler);
        rpcServer.setMaxIdleTime(3000);
        rpcServer.start();
    }
}
