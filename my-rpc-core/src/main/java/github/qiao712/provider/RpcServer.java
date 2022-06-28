package github.qiao712.provider;

import github.qiao712.entity.RpcRequest;
import github.qiao712.entity.RpcResponse;
import github.qiao712.exception.RpcException;
import github.qiao712.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class RpcServer {
    private final int port;
    private final Executor executor = Executors.newCachedThreadPool();
    private final ServiceRegistry serviceRegistry;
    private final RequestHandler requestHandler;

    /**
     * 处理一个请求的任务
     */
    private class HandleRequestTask implements Runnable{
        private final Socket clientSocket;

        final int bufferSize = 1024;
        private final byte[] buffer = new byte[bufferSize];

        public HandleRequestTask(Socket clientSocket){
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try(ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream())) {

                //获取请求
                Object requestObject = objectInputStream.readObject();
                RpcRequest rpcRequest = null;
                if(requestObject instanceof RpcRequest){
                    rpcRequest = (RpcRequest) requestObject;
                }else{
                    log.info("非法请求");
                }

                //处理请求
                RpcResponse rpcResponse = requestHandler.handleRequest(rpcRequest);
                
                //返回
                objectOutputStream.writeObject(rpcResponse);
            } catch (IOException | ClassNotFoundException e) {
                log.info("请求获取失败", e);
            } finally {
                //关闭连接
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    log.error("Socket关闭异常", e);
                }
            }
        }
    }

    public RpcServer(int port, ServiceRegistry serviceRegistry) {
        this.port = port;
        this.serviceRegistry = serviceRegistry;
        this.requestHandler = new DefaultRequestHandler(serviceRegistry);
    }

    public void start(){
        //接收请求,并交给处理线程
        executor.execute(() -> {
            ServerSocket serverSocket;
            try {
                serverSocket = new ServerSocket(port);

                log.info("在端口{}上监听", port);

                while(true){
                    Socket clientSocket = serverSocket.accept();
                    executor.execute(new HandleRequestTask(clientSocket));
                }
            } catch (IOException e) {
                throw new RpcException("启动失败", e);
            }
        });
    }
}
