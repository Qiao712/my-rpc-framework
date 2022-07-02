package github.qiao712.rpc.transport.netty.client;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.transport.netty.client.ClientChannelInitializer;
import github.qiao712.rpc.transport.netty.client.NettyRpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 线程安全的客户端的简易连接池
 */
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
     */
    public Channel getChannel(String host, int port) {
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);

        return channels.computeIfAbsent(socketAddress, new Function<SocketAddress, Channel>() {
            @Override
            public Channel apply(SocketAddress socketAddress1) {
                try {
                    ChannelFuture channelFuture = bootstrap.connect(socketAddress1);
                    return channelFuture.sync().channel();
                } catch (Throwable e) {
                    throw new RpcException("无法连接至服务器", e);
                }
            };
        });
    }

    /**
     * 关闭并移除连接
     */
    public Channel removeChannel(String host, int port){
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        Channel channel = channels.remove(socketAddress);
        if(channel != null){
            ChannelFuture channelFuture = channel.close();
            //异步的关闭
        }
        return channel;
    }
}
