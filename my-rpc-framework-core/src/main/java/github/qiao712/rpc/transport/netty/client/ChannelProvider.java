package github.qiao712.rpc.transport.netty.client;

import github.qiao712.rpc.exception.RpcClientException;
import github.qiao712.rpc.proto.Message;
import github.qiao712.rpc.transport.netty.RpcMessageCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * 客户端的简易连接池
 */
@Slf4j
public class ChannelProvider {
    private final Bootstrap bootstrap = new Bootstrap();
    private final Map<SocketAddress, Channel> channels = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    private final int reconnectInterval = 5000;    //重试间隔 1min
    private final int maxReconnectTime = 10;   //最大重试次数

    public ChannelProvider(NettyRpcClient nettyRpcClient){
        //配置客户端Bootstrap
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(new IdleStateHandler(nettyRpcClient.getHeartbeatInterval(), 0, 0, TimeUnit.MILLISECONDS));
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Message.MAX_LENGTH, 4, 4, -8, 0));
                        pipeline.addLast(new RpcMessageCodec());
                        pipeline.addLast(new ClientMessageInboundHandler(nettyRpcClient.getWaitingRequestPool(), nettyRpcClient.getChannelProvider()));
                    }
                });
    }

    /**
     * 从池中取得channel或建立新channel
     * 若channel失效，将重新连接
     */
    public Channel getChannel(SocketAddress socketAddress) {
        return channels.compute(socketAddress, new BiFunction<SocketAddress, Channel, Channel>() {
            @Override
            public Channel apply(SocketAddress socketAddress, Channel channel) {
                if(channel == null || !channel.isActive()){
                    log.debug("与{}建立连接", socketAddress);
                    //连接不存在 或 已失效，则建立新连接
                    try {
                        ChannelFuture channelFuture = bootstrap.connect(socketAddress);
                        return channelFuture.sync().channel();
                    } catch (Throwable e) {
                        throw new RpcClientException("无法连接至" + socketAddress, e);
                    }
                }else{
                    return channel;
                }
            }
        });
    }

    /**
     * 关闭并移除连接
     */
    public void removeChannel(SocketAddress socketAddress){
        Channel channel = channels.remove(socketAddress);
        if(channel != null && (channel.isOpen() || channel.isActive())){
            channel.close();
        }
    }

    /**
     * 关闭并移除连接
     */
    public void removeChannel(Channel channel){
        if(channel == null) return;
        channels.remove(channel.remoteAddress());
        channel.close();
    }

    /**
     * 定时尝试重连
     */
    public void tryReconnect(SocketAddress socketAddress){
        Runnable reconnect = new Runnable() {
            private int retry = 0;  //已重试次数

            @Override
            public void run() {
                try {
                    retry++;
                    getChannel(socketAddress);
                } catch (RpcClientException e) {
                    log.debug("与" + socketAddress + "重连失败", e);

                    if(retry < maxReconnectTime){
                        scheduledExecutorService.schedule(this, reconnectInterval, TimeUnit.MILLISECONDS);
                    }else{
                        log.debug("已经重试{}次, 放弃连接.", maxReconnectTime);
                    }
                }
            }
        };

        scheduledExecutorService.schedule(reconnect, reconnectInterval, TimeUnit.MILLISECONDS);
    }
}
