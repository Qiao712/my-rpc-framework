package github.qiao712.test.consumer;

import github.qiao712.processor.RpcServiceBeanPostProcessor;
import github.qiao712.rpc.proto.SerializationType;
import github.qiao712.rpc.proxy.JDKRpcProxyFactory;
import github.qiao712.rpc.proxy.RpcProxyFactory;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;
import github.qiao712.rpc.transport.netty.client.NettyRpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.net.InetSocketAddress;

@SpringBootApplication
public class ConsumerApplication {
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(ConsumerApplication.class, args);

        LocalService localService = context.getBean(LocalService.class);
        localService.testRpc();

        Thread.sleep(100000);
    }

    @Bean
    public RpcClient rpcClient(){
        RpcClient rpcClient = new NettyRpcClient();
        rpcClient.setSerializationType(SerializationType.HESSIAN_SERIALIZATION);
        rpcClient.setResponseTimeout(1000L);
        return rpcClient;
    }

    @Bean
    public ServiceDiscovery serviceDiscovery(){
        return new ZookeeperServiceDiscovery(new InetSocketAddress("8.141.151.176", 2181));
    }

    @Bean
    public RpcProxyFactory proxyFactory(){
        return new JDKRpcProxyFactory();
    }

    @Bean
    public RpcServiceBeanPostProcessor rpcServiceBeanPostProcessor(){
        return new RpcServiceBeanPostProcessor();
    }
}
