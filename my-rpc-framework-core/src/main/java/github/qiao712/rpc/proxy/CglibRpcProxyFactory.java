package github.qiao712.rpc.proxy;

import github.qiao712.rpc.cluster.Cluster;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 基于 CGLib 实现的 RPC 代理工厂
 */
public class CglibRpcProxyFactory implements RpcProxyFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<?> serviceClass, String serviceName, Cluster cluster) {
        Enhancer enhancer = new Enhancer();
        // 设置父类为服务接口
        enhancer.setSuperclass(serviceClass);
        // 设置回调方法拦截器
        enhancer.setCallback(new CglibRpcMethodInterceptor(serviceName, cluster));

        return (T) enhancer.create();
    }

    /**
     * CGLib 方法拦截器，负责拦截方法调用并转发给集群
     */
    private static class CglibRpcMethodInterceptor implements MethodInterceptor {
        private final String serviceName;
        private final Cluster cluster;

        public CglibRpcMethodInterceptor(String serviceName, Cluster cluster) {
            this.serviceName = serviceName;
            this.cluster = cluster;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            // 如果是 Object 类的方法（如 toString, hashCode），直接调用原方法或特殊处理
            if (Object.class.equals(method.getDeclaringClass())) {
                return proxy.invokeSuper(obj, args);
            }

            // 调用集群执行远程逻辑
            // 注意：这里复用了您现有 JDK 代理中的 cluster.invoke 逻辑
            return cluster.invoke(serviceName, method.getName(), args, method.getParameterTypes());
        }
    }
}
