package github.qiao712.rpc.transport.netty.client;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.*;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.AbstractRpcClient;
import github.qiao712.rpc.util.AutoIncrementIdGenerator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import sun.nio.ch.Net;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NettyRpcClient extends AbstractRpcClient {
    private final WaitingRequestPool waitingRequestPool = new WaitingRequestPool();
    private final AutoIncrementIdGenerator idGenerator = new AutoIncrementIdGenerator();
    private final ClientChannelPool clientChannelPool = new ClientChannelPool(this);

    public NettyRpcClient(){
    }

    @Override
    public RpcResponse request(InetSocketAddress providerAddress, RpcRequest rpcRequest) {
        Channel channel = clientChannelPool.getChannel(providerAddress);

        Message<RpcRequest> requestMessage = new Message<>();
        requestMessage.setMessageType(MessageType.REQUEST);
        requestMessage.setSerializationType(serializationType);
        requestMessage.setPayload(rpcRequest);
        requestMessage.setRequestId(idGenerator.generateId());

        //发送请求
        ChannelFuture channelFuture = channel.writeAndFlush(requestMessage);
        try {
            channelFuture.sync();
        } catch (InterruptedException e) {
            throw new RpcException("请求发送失败", e);
        }

        //在与该request id关联的CompletableFuture上等待响应
        CompletableFuture<RpcResponse> responseFuture = waitingRequestPool.waitResponse(requestMessage.getRequestId());
        try {
            return responseTimeout > 0 ? responseFuture.get(responseTimeout, TimeUnit.MILLISECONDS) : responseFuture.get();
        } catch (InterruptedException e) {
            throw new RpcException("请求被中断", e);
        } catch (TimeoutException e) {
            waitingRequestPool.abandonRequest(requestMessage.getRequestId());
            throw new RpcException("响应超时", e);
        } catch (ExecutionException e) {
            throw new RpcException("响应获取失败", e.getCause());
        }
    }

    public WaitingRequestPool getWaitingRequestPool(){
        return waitingRequestPool;
    }

    public ClientChannelPool getClientChannelPool(){
        return clientChannelPool;
    }
}
