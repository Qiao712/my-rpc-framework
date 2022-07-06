package github.qiao712.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JDKRpcProxyFactory implements RpcProxyFactory {
    private final Invoker invoker;

    public JDKRpcProxyFactory(Invoker invoker){
        this.invoker = invoker;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> cls) {
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, new RpcProxyInvocationHandler(cls.getCanonicalName()));
    }

    private class RpcProxyInvocationHandler implements InvocationHandler {
        private final String serviceName;

        public RpcProxyInvocationHandler(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args){
            return invoker.invoke(serviceName, method.getName(), args);
        }

    }
}
