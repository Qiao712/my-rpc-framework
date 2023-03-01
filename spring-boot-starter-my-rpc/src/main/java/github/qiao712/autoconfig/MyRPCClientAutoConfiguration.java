package github.qiao712.autoconfig;

import github.qiao712.processor.RpcServiceReferenceBeanPostProcessor;
import github.qiao712.rpc.proto.SerializationType;
import github.qiao712.rpc.proxy.RpcProxyFactory;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.registry.zookeeper.CuratorUtils;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceRegistry;
import github.qiao712.rpc.transport.RpcClient;
import github.qiao712.rpc.transport.netty.client.NettyRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 客户端相关组件注册
 */
@Configuration
@EnableConfigurationProperties(MyRPCProperties.class)   //注册储存配置值的Bean
public class MyRPCClientAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MyRPCClientAutoConfiguration.class);

    @Resource
    MyRPCProperties properties;

    /**
     * 服务注册与发现组件
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceRegistry serviceRegistry(){
        if(properties.getRegistryAddresses() != null && properties.getRegistryAddresses().length != 0){
            return new ZookeeperServiceRegistry(CuratorUtils.getAddressString(properties.getRegistryAddresses()));
        }
        logger.error("未配置注册中新地址");
        return null;
    }

    /**
     * Netty客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public NettyRpcClient nettyRpcClient(){
        NettyRpcClient rpcClient = new NettyRpcClient();

        //配置序列化方式
        String serializer = properties.getSerializer().toUpperCase();
        for (SerializationType value : SerializationType.values()) {
            if (value.name().equals(serializer + "_SERIALIZATION")){
                rpcClient.setSerializationType(value);
                break;
            }
        }

        //全局的调用超时时间
        rpcClient.setResponseTimeout(Math.max(properties.getResponseTimeout(), 0));

        return rpcClient;
    }

    /**
     * 创建代理对象的工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public RpcProxyFactory rpcProxyFactory(){
        try{
            Class<?> clazz = Class.forName(properties.getProxyFactory());
            Constructor<?> constructor = clazz.getConstructor();
            Object newInstance = constructor.newInstance();
            if(!(newInstance instanceof RpcProxyFactory)){
                throw new BeanCreationException(properties.getProxyFactory() + "不是RpcProxyFactory");
            }
            return (RpcProxyFactory) newInstance;
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new BeanCreationException("创建RpcProxyFactory失败", e);
        }
    }

    /**
     * Bean后处理器
     * 用于注入注解了@RpcServiceReference的属性
     */
    @Bean
    @ConditionalOnMissingBean
    public RpcServiceReferenceBeanPostProcessor rpcServiceReferenceBeanPostProcessor(ServiceRegistry serviceRegistry,
                                                                                     RpcClient rpcClient,
                                                                                     RpcProxyFactory rpcProxyFactory){
        return new RpcServiceReferenceBeanPostProcessor(rpcProxyFactory, rpcClient, serviceRegistry);
    }
}
