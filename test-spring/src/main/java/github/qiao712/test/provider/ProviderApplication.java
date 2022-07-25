package github.qiao712.test.provider;

import github.qiao712.annotation.EnableRpcServiceScan;
import github.qiao712.processor.RpcServiceProcessor;
import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.handler.SimpleRequestHandler;
import github.qiao712.rpc.registry.ServiceProvider;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceRegistry;
import github.qiao712.rpc.transport.RpcServer;
import github.qiao712.rpc.transport.netty.server.NettyRpcServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.net.InetSocketAddress;

@SpringBootApplication
@EnableRpcServiceScan({"github.qiao712.test"})
public class ProviderApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ProviderApplication.class, args);
    }

    //创建 ServiceRegistry、ServiceProvider
    @Bean
    public ServiceProvider serviceProvider(){
        ServiceRegistry serviceRegistry = new ZookeeperServiceRegistry(new InetSocketAddress("127.0.0.1", 9712), new InetSocketAddress("8.141.151.176", 2181));
        ServiceProvider serviceProvider = new ServiceProvider(serviceRegistry);
        return serviceProvider;
    }

    //创建RpcServer 并启动
    @Bean
    public RpcServer rpcServer(ServiceProvider serviceProvider){
        RequestHandler requestHandler = new SimpleRequestHandler(serviceProvider);
        NettyRpcServer rpcServer = new NettyRpcServer(9712, requestHandler);
        rpcServer.start();
        return rpcServer;
    }
}
