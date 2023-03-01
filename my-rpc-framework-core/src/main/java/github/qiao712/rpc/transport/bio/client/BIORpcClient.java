package github.qiao712.rpc.transport.bio.client;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.proto.Message;
import github.qiao712.rpc.proto.MessageType;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.transport.AbstractRpcClient;
import github.qiao712.rpc.transport.bio.RpcMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class BIORpcClient extends AbstractRpcClient {
    private final RpcMessageCodec rpcMessageCodec = new RpcMessageCodec();

    public BIORpcClient(){
    }

    @Override
    public RpcResponse request(InetSocketAddress providerAddress, RpcRequest rpcRequest) {
        try(Socket socket = new Socket()){
            socket.connect(providerAddress);

            try(BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
                BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream())){
                //发送请求
                Message<RpcRequest> requestMessage = new Message<>();
                requestMessage.setMessageType(MessageType.REQUEST);
                //requestMessage.setRequestId(0);   request id 对一个连接处理一个请求的实现无意义
                requestMessage.setSerializationType(serializationType);
                requestMessage.setPayload(rpcRequest);
                rpcMessageCodec.encodeMessage(requestMessage, outputStream);
                outputStream.flush();

                //等待响应
                Message<Object> responseMessage = rpcMessageCodec.decodeMessage(inputStream);

                if(!(responseMessage.getPayload() instanceof RpcResponse)){
                    throw new RpcException("响应格式错误");
                }

                return (RpcResponse) responseMessage.getPayload();
            } catch (IOException e) {
                throw new RpcException("请求失败", e);
            }
        } catch (IOException e) {
            log.debug("无法连接至{}", providerAddress);
            throw new RpcException("无法连接至服务提供者", e);
        }
    }
}
