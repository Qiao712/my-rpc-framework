package github.qiao712.rpc.transport.netty.server;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.proto.Message;
import github.qiao712.rpc.proto.SerializationType;
import github.qiao712.rpc.transport.AbstractRpcServer;
import github.qiao712.rpc.transport.netty.RpcMessageCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcServer extends AbstractRpcServer {
    private final int port;
    private final ServerBootstrap serverBootstrap = new ServerBootstrap();
    private final EventLoopGroup bossLoopGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerLoopGroup = new NioEventLoopGroup();

    public NettyRpcServer(int port, RequestHandler requestHandler){
        this(port, requestHandler, SerializationType.JDK_SERIALIZATION);
    }

    public NettyRpcServer(int port, RequestHandler requestHandler, SerializationType serializationType) {
        super(requestHandler, serializationType);
        this.port = port;

        serverBootstrap.group(bossLoopGroup, workerLoopGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(new IdleStateHandler(0, 0, maxIdleTime, TimeUnit.MILLISECONDS));
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Message.MAX_LENGTH, 4, 4, -8, 0));
                        pipeline.addLast(new RpcMessageCodec());
                        pipeline.addLast(new ServerMessageInboundHandler(requestHandler, serializationType));
                    }
                });
    }


    @Override
    public void start() {
        try {
            serverBootstrap.bind(port).sync();
            log.info("在端口{}上监听", port);
        } catch (Throwable e){
            log.error("服务器启动失败", e);
            throw new RpcException("服务器启动失败:", e);
        }
    }
}
