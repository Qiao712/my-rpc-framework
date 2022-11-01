package github.qiao712.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 使用@ConfiguratonProperties注解将外部配置文件中定义的值绑定到该用于储存配置值的Bean上
 */
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String[] getRegistryAddresses() {
        return registryAddresses;
    }

    public void setRegistryAddresses(String[] registryAddresses) {
        this.registryAddresses = registryAddresses;
    }

    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public String getRequestHandler() {
        return requestHandler;
    }

    public void setRequestHandler(String requestHandler) {
        this.requestHandler = requestHandler;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public long getThreadKeepaliveTime() {
        return threadKeepaliveTime;
    }

    public void setThreadKeepaliveTime(int threadKeepaliveTime) {
        this.threadKeepaliveTime = threadKeepaliveTime;
    }

    public long getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(long responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public String getProxyFactory() {
        return proxyFactory;
    }

    public void setProxyFactory(String proxyFactory) {
        this.proxyFactory = proxyFactory;
    }
}
