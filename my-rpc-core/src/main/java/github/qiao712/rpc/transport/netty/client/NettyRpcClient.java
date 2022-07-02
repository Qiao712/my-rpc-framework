package github.qiao712.rpc.transport.netty.client;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.proto.Message;
import github.qiao712.rpc.proto.MessageType;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.transport.AbstractRpcClient;
import github.qiao712.rpc.util.AutoIncrementIdGenerator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class NettyRpcClient extends AbstractRpcClient {
    private String host;
    private int port;
    private final WaitingRequestPool waitingRequestPool = new WaitingRequestPool();
    private final AutoIncrementIdGenerator idGenerator = new AutoIncrementIdGenerator();
    private final ClientChannelPool clientChannelPool = new ClientChannelPool(this);

    public NettyRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public RpcResponse request(RpcRequest rpcRequest) {
        Channel channel = clientChannelPool.getChannel(host, port);

        Message<RpcRequest> requestMessage = new Message<>();
        requestMessage.setMessageType(MessageType.REQUEST);
        requestMessage.setSerializationType(serializationType);
        requestMessage.setPayload(rpcRequest);
        requestMessage.setRequestId(idGenerator.generateId());

        ChannelFuture channelFuture = channel.writeAndFlush(requestMessage);
        try {
            channelFuture.sync();
        } catch (InterruptedException e) {
            throw new RpcException("请求发送失败", e);
        }

        CompletableFuture<RpcResponse> responseFuture = waitingRequestPool.waitResponse(requestMessage.getRequestId());
        try {
            return responseFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RpcException("响应获取失败", e);
        }
    }

    public WaitingRequestPool getWaitingRequestPool(){
        return waitingRequestPool;
    }
}
