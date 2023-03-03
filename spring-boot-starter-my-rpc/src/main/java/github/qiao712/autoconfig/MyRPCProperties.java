package github.qiao712.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 使用@ConfiguratonProperties注解将外部配置文件中定义的值绑定到该用于储存配置值的Bean上
 */
@Data
@ConfigurationProperties(prefix = "my-rpc")
public class MyRPCProperties {
    //注册中心的地址和端口--多个Zookeeper节点的地址
    private String[] registryAddresses;
    //序列化器
    private String serializer = "Hessian";


    /**
     * 服务提供者相关配置
     */
    //提供服务的地址和端口
    private String host = "localhost";
    private int port = 9712;
    //应用名称
    private String applicationName;
    //连接最大空闲时间 ms
    private long maxIdleTime = -1;
    //请求处理器
    private String requestHandler = "github.qiao712.rpc.handler.ConcurrentRequestHandler";
    //当使用ConcurrentRequestHandler时，配置线程池
    private int corePoolSize = Runtime.getRuntime().availableProcessors();
    private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;
    private int queueSize = -1;
    private long threadKeepaliveTime = 60000;

    /**
     * 消费者相关配置
     */
    //全局的调用超时时间
    private long responseTimeout = -1;
    //创建代理对象的方式
    private String proxyFactory = "github.qiao712.rpc.proxy.JDKRpcProxyFactory";
}
