package github.qiao712.consumer.client;

import github.qiao712.entity.RpcRequest;
import github.qiao712.entity.RpcResponse;
import github.qiao712.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class BIORpcClient extends AbstractRpcClient{
    private final InetSocketAddress serverAddress;

    public BIORpcClient(String host, int port) {
        this.serverAddress = new InetSocketAddress(host, port);
    }

    @Override
    public RpcResponse request(RpcRequest rpcRequest) {
        RpcResponse rpcResponse = null;

        try(Socket socket = new Socket()){
            socket.connect(serverAddress);

            try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())){

                //发送请求
                objectOutputStream.writeObject(rpcRequest);
                //等待响应
                Object returnObject = objectInputStream.readObject();

                if(!(returnObject instanceof RpcResponse)){
                    throw new RpcException("响应格式错误");
                }

                rpcResponse = (RpcResponse) returnObject;
            } catch (IOException | ClassNotFoundException e) {
                throw new RpcException("请求失败", e);
            }
        } catch (IOException e) {
            log.debug("无法连接至{}", serverAddress);
            throw new RpcException("无法连接至服务提供者", e);
        }

        return rpcResponse;
    }
}
