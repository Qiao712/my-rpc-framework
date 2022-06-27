package github.qiao712;

import qiao712.domain.RpcRequest;
import qiao712.domain.RpcResponse;
import qiao712.domain.RpcResponseCode;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RpcClient {
    private final InetSocketAddress serverAddress;

    public RpcClient(String host, int port) {
        this.serverAddress = new InetSocketAddress(host, port);
    }

    /**
     * 传入服务类的接口，获取代理对象
     */
    public <T> T getProxy(Class<T> serviceClass){
        return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[]{serviceClass}, new ServiceInvokeHandler());
    }

    private class ServiceInvokeHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            RpcRequest rpcRequest = new RpcRequest(method.getDeclaringClass(), method.getName(), args);

            Socket socket = new Socket();
            socket.connect(serverAddress);

//            System.out.println("[已连接]");

            Object returnVal = null;
            try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())){
                //发送请求
                objectOutputStream.writeObject(rpcRequest);

                //等待响应
                Object returnObject = objectInputStream.readObject();

//                System.out.println("[获得响应]");

                if(!(returnObject instanceof RpcResponse)){
                    throw new RuntimeException("请求失败");
                }

                RpcResponse response = (RpcResponse) returnObject;
                if(response.getCode() != RpcResponseCode.SUCCESS){
                    throw new RuntimeException("请求失败:" + response.getCode());
                }

                returnVal = response.getData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            socket.close();

//            System.out.println("[关闭连接]");

            return returnVal;
        }
    }
}
