package github.qiao712.rpc.cluster;

/**
 * 抽象一个集群
 * 提供容错功能，整合服务发现、负载均衡、发送调用请求等操作
 * 被客户端的代理所引用，发起调用
 */
public interface Cluster {
    /**
     * 发起远程调用
     * @param serviceName 服务名
     * @param methodName 方法名
     * @param args 参数
     * @param argTypes 参数类型
     * @return
     */
    Object invoke(String serviceName, String methodName, Object[] args, Class<?>[] argTypes);
}
