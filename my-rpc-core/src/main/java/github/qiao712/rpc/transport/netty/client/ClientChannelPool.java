package github.qiao712.rpc.transport.netty.client;

import github.qiao712.rpc.exception.RpcException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * 客户端的简易连接池
 * 线程安全的
 */
@Slf4j
public class ClientChannelPool {
    private final NettyRpcClient nettyRpcClient;
    private final Bootstrap bootstrap = new Bootstrap();
    private final Map<SocketAddress, Channel> channels = new ConcurrentHashMap<>();

    public ClientChannelPool(NettyRpcClient nettyRpcClient){
        this.nettyRpcClient = nettyRpcClient;

        //配置客户端Bootstrap
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ClientChannelInitializer(nettyRpcClient));
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
                        throw new RpcException("无法连接至" + socketAddress, e);
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
    public Channel removeChannel(InetSocketAddress socketAddress){
        Channel channel = channels.remove(socketAddress);
        if(channel != null && (channel.isOpen() || channel.isActive())){
            ChannelFuture channelFuture = channel.close();
        }
        return channel;
    }
}
