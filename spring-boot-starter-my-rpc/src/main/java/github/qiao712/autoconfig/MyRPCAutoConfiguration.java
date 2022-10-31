package github.qiao712.autoconfig;

import github.qiao712.processor.RpcServiceBeanPostProcessor;
import github.qiao712.rpc.handler.ConcurrentRequestHandler;
import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.handler.SerializableRequestHandler;
import github.qiao712.rpc.handler.SimpleRequestHandler;
import github.qiao712.rpc.proto.SerializationType;
import github.qiao712.rpc.registry.ServiceProvider;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.registry.zookeeper.CuratorUtils;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceRegistry;
import github.qiao712.rpc.transport.netty.server.NettyRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

@Configuration
@EnableConfigurationProperties(MyRPCProperties.class)   //注册储存配置值的Bean
public class MyRPCAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MyRPCAutoConfiguration.class);

    @Resource
    MyRPCProperties properties;

    //服务提供者相关组件注册--------------------------------------------------
    /**
     * 服务注册组件
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceRegistry serviceRegistry(){
        if(properties.getRegistryAddresses() != null && properties.getRegistryAddresses().length != 0){
            InetSocketAddress providerAddress = new InetSocketAddress(properties.getHost(), properties.getPort());
            return new ZookeeperServiceRegistry(providerAddress, CuratorUtils.getAddressString(properties.getRegistryAddresses()));
        }
        logger.error("未配置注册中新地址");
        return null;
    }

    /**
     * 用于保证提供的服务的信息，并通过ServiceRegistry进行注册
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceProvider serviceProvider(ServiceRegistry serviceRegistry){
        if(serviceRegistry != null){
            return new ServiceProvider(serviceRegistry);
        }
        return null;
    }

    /**
     * Request处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public RequestHandler requestHandler(ServiceProvider serviceProvider){
        String requestHandlerClassName = properties.getRequestHandler();
        if(SimpleRequestHandler.class.getCanonicalName().equals(requestHandlerClassName)){
            return new SimpleRequestHandler(serviceProvider);
        }else if(ConcurrentRequestHandler.class.getCanonicalName().equals(requestHandlerClassName)){
            ExecutorService executorService = new ThreadPoolExecutor(
                    properties.getCorePoolSize(),
                    properties.getMaxPoolSize(),
                    properties.getThreadKeepaliveTime(),
                    TimeUnit.MILLISECONDS,
                    properties.getQueueSize() < 1 ? new LinkedBlockingQueue<>() : new ArrayBlockingQueue<>(properties.getQueueSize()),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
            return new ConcurrentRequestHandler(serviceProvider, executorService);
        }else if(SerializableRequestHandler.class.getCanonicalName().equals(requestHandlerClassName)){
            if(properties.getQueueSize() > 0){
                return new SerializableRequestHandler(serviceProvider, properties.getQueueSize());
            }else{
                return new SerializableRequestHandler(serviceProvider);
            }
        }
        return null;
    }

    /**
     * 用于自动注册的BeanPostProcessor
     */
    @Bean
    @ConditionalOnMissingBean
    public RpcServiceBeanPostProcessor rpcServiceBeanPostProcessor(ServiceProvider serviceProvider){
        return new RpcServiceBeanPostProcessor(serviceProvider);
    }

    /**
     * 启动Netty服务端
     */
    @Bean
    @ConditionalOnMissingBean
    public NettyRpcServer nettyRpcServer(RequestHandler requestHandler){
        NettyRpcServer rpcServer = new NettyRpcServer(properties.getPort(), requestHandler);

        //最大空闲时间
        rpcServer.setMaxIdleTime(properties.getMaxIdleTime() <= 0 ? 0 : properties.getMaxIdleTime());

        //配置序列化方式
        String serializer = properties.getSerializer().toUpperCase();
        for (SerializationType value : SerializationType.values()) {
            if (value.name().equals(serializer + "_SERIALIZATION")){
                rpcServer.setSerializationType(value);
                break;
            }
        }

        rpcServer.start();
        return rpcServer;
    }

    //服务消费者相关组件注册--------------------------------------------------

}
