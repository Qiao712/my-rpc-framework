package github.qiao712.provider.server.bio;

import github.qiao712.exception.RpcException;
import github.qiao712.proto.*;
import github.qiao712.provider.RequestHandler;
import github.qiao712.provider.server.RpcServer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class BIORpcServer implements RpcServer {
    private final int port;
    private final Executor executor = Executors.newCachedThreadPool();
    private final RequestHandler requestHandler;
    private final MessageCoder messageCoder = new MessageCoder();

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
            try(BufferedInputStream inputStream = new BufferedInputStream(clientSocket.getInputStream());
                BufferedOutputStream outputStream = new BufferedOutputStream(clientSocket.getOutputStream())) {
                //获取请求
                Message<Object> requestMessage = messageCoder.decodeMessage(inputStream);
                RpcRequest rpcRequest;
                if(requestMessage.getPayload() instanceof RpcRequest){
                    rpcRequest = (RpcRequest) requestMessage.getPayload();
                }else{
                    log.info("非法请求");
                    throw new RpcException("非法请求");
                }

                //处理请求
                RpcResponse rpcResponse = requestHandler.handleRequest(rpcRequest);

                //返回
                Message<RpcResponse> responseMessage = new Message<>();
                responseMessage.setMessageType(MessageType.RESPONSE);
                responseMessage.setSerializationType(requestMessage.getSerializationType());
                responseMessage.setPayload(rpcResponse);
                messageCoder.encodeMessage(responseMessage, outputStream);
            } catch (IOException e) {
                log.error("Socket I/O 异常", e);
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

    public BIORpcServer(int port, RequestHandler requestHandler) {
        this.port = port;
        this.requestHandler = requestHandler;
    }

    @Override
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
