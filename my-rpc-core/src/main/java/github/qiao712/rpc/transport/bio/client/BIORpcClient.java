package github.qiao712.rpc.transport.bio.client;

import github.qiao712.rpc.proto.*;
import github.qiao712.rpc.transport.AbstractRpcClient;
import github.qiao712.rpc.transport.bio.MessageCodec;
import github.qiao712.proto.*;
import github.qiao712.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class BIORpcClient extends AbstractRpcClient {
    private final InetSocketAddress serverAddress;
    private final MessageCodec messageCodec = new MessageCodec();
    private SerializationType serializationType = SerializationType.JDK_SERIALIZATION;        //所使用的序列化方式

    public BIORpcClient(String host, int port) {
        this.serverAddress = new InetSocketAddress(host, port);
    }

    @Override
    public RpcResponse request(RpcRequest rpcRequest) {
        try(Socket socket = new Socket()){
            socket.connect(serverAddress);

            try(BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
                BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream())){
                //发送请求
                Message<RpcRequest> requestMessage = new Message<>();
                requestMessage.setMessageType(MessageType.REQUEST);
                requestMessage.setSerializationType(serializationType);
                requestMessage.setPayload(rpcRequest);
                messageCodec.encodeMessage(requestMessage, outputStream);
                outputStream.flush();

                //等待响应
                Message<Object> responseMessage = messageCodec.decodeMessage(inputStream);

                if(!(responseMessage.getPayload() instanceof RpcResponse)){
                    throw new RpcException("响应格式错误");
                }

                return (RpcResponse) responseMessage.getPayload();
            } catch (IOException e) {
                throw new RpcException("请求失败", e);
            }
        } catch (IOException e) {
            log.debug("无法连接至{}", serverAddress);
            throw new RpcException("无法连接至服务提供者", e);
        }
    }

    @Override
    public void setSerializationType(SerializationType serializationType) {
        this.serializationType = serializationType;
    }

    @Override
    public SerializationType getSerializationType() {
        return serializationType;
    }
}
