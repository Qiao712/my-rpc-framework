package github.qiao712.rpc.transport.bio.server;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.exception.RpcServerException;
import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.handler.ResponseSender;
import github.qiao712.rpc.proto.Message;
import github.qiao712.rpc.proto.MessageType;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.transport.AbstractRpcServer;
import github.qiao712.rpc.transport.bio.RpcMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class BIORpcServer extends AbstractRpcServer {
    private final int port;
    private final Executor executor = Executors.newCachedThreadPool();
    private final RpcMessageCodec rpcMessageCodec = new RpcMessageCodec();

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
                Message<Object> requestMessage = rpcMessageCodec.decodeMessage(inputStream);
                RpcRequest rpcRequest;
                if(requestMessage.getPayload() instanceof RpcRequest){
                    rpcRequest = (RpcRequest) requestMessage.getPayload();
                }else{
                    log.info("非法请求");
                    throw new RpcException("非法请求");
                }

                //用于发送请求的回调
                ResponseSender responseSender = new ResponseSender() {
                    @Override
                    public void send(RpcResponse rpcResponse) {
                        //返回
                        Message<RpcResponse> responseMessage = new Message<>();
                        responseMessage.setMessageType(MessageType.RESPONSE);
                        responseMessage.setSerializationType(serializationType);
                        responseMessage.setRequestId(requestMessage.getRequestId());    //消费者的实现可能是需要request id的实现(Netty)
                        responseMessage.setPayload(rpcResponse);
                        rpcMessageCodec.encodeMessage(responseMessage, outputStream);
                    }
                };

                requestHandler.handleRequest(rpcRequest, responseSender);
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

    public BIORpcServer(int port, RequestHandler requestHandler){
        super(requestHandler);
        this.port = port;
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
                throw new RpcServerException("启动失败", e);
            }
        });
    }
}
