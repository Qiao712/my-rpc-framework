package github.qiao712.rpc.proxy;

import github.qiao712.rpc.cluster.Cluster;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JDKRpcProxyFactory implements RpcProxyFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<?> serviceClass, String serviceName, Cluster cluster) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new RpcProxyInvocationHandler(serviceName, cluster));
    }

    private static class RpcProxyInvocationHandler implements InvocationHandler {
        private final String serviceName;
        private final Cluster cluster;

        private RpcProxyInvocationHandler(String serviceName, Cluster cluster) {
            this.serviceName = serviceName;
            this.cluster = cluster;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args){
            return cluster.invoke(serviceName, method.getName(), args);
        }
    }
}
