package github.qiao712.server;

import qiao712.domain.RpcRequest;
import qiao712.domain.RpcResponse;
import qiao712.domain.RpcResponseCode;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RpcServer {
    private final int port;
    private final Executor executor = Executors.newCachedThreadPool();

    /**
     * 服务
     */
    private final ConcurrentMap<Class<?>, Object> services = new ConcurrentHashMap<>();

    /**
     * 处理一个请求的任务
     */
    private class HandlerRequest implements Runnable{
        private final Socket clientSocket;
        final int bufferSize = 1024;
        private final byte[] buffer = new byte[bufferSize];

        public HandlerRequest(Socket clientSocket){
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try(ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream())){

                Object requestObject = objectInputStream.readObject();
                RpcRequest rpcRequest;
                if(requestObject instanceof RpcRequest){
                    rpcRequest = (RpcRequest) requestObject;
                }else{
                    throw new RuntimeException("非法请求");
                }

                Class<?> serviceType = rpcRequest.getServiceType();
                Object service = services.get(rpcRequest.getServiceType());
                if(service == null){
                    //响应: 未找到对应的服务错误
                    objectOutputStream.writeObject(RpcResponse.bad(RpcResponseCode.SERVICE_NOT_FOUND));
                    return;
                }

                //调用
                Object[] arguments = rpcRequest.getArgs();
                int argumentCount = arguments == null ? 0 : arguments.length;
                Class<?>[] argumentTypes = new Class<?>[argumentCount];
                for (int i = 0; i < argumentCount; i++) {
                    argumentTypes[i] =arguments[i].getClass();
                }

                Method method = serviceType.getMethod(rpcRequest.getMethodName(), argumentTypes);
                Object returnVal = method.invoke(service, rpcRequest.getArgs());

                //返回
                objectOutputStream.writeObject(RpcResponse.ok(returnVal));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("非法请求", e);
            } catch (InvocationTargetException | IOException | IllegalAccessException e) {
                throw new RuntimeException("处理请求失败", e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("请求处理失败(找不到方法)", e);
            }

            //关闭连接
            try {
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public RpcServer(int port){
        this.port = port;
    }

    public void start(){
        //接收请求,并交给处理线程
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket;
                try {
                    //TODO: 规范化错误处理
                    serverSocket = new ServerSocket(port);
                    while(true){
                        Socket clientSocket = serverSocket.accept();
                        executor.execute(new HandlerRequest(clientSocket));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("启动失败", e);
                }
            }
        });
    }

    public void register(Object service){
        Class<?>[] interfaces = service.getClass().getInterfaces();
        for (Class<?> anInterface : interfaces) {
            services.put(anInterface, service);
        }
    }
}
