package github.qiao712.consumer.proxy;

import github.qiao712.consumer.client.RpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JDKRpcProxyFactory implements RpcProxyFactory {
    private final RpcClient rpcClient;

    public JDKRpcProxyFactory(RpcClient rpcClient){
        this.rpcClient = rpcClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(String serviceName, Class<T> cls) {
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, new RpcInvokeHandler(serviceName));
    }

    private class RpcInvokeHandler implements InvocationHandler {
        private final String serviceName;

        public RpcInvokeHandler(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args){
            return rpcClient.invoke(serviceName, method.getName(), args);
        }

    }
}
