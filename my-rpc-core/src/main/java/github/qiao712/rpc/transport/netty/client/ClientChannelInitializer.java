package github.qiao712.rpc.transport.netty.client;

import github.qiao712.rpc.proto.Message;
import github.qiao712.rpc.transport.netty.RpcMessageCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 配置消费者一侧的 ChannelPipeline
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final NettyRpcClient nettyRpcClient;

    public ClientChannelInitializer(NettyRpcClient nettyRpcClient) {
        this.nettyRpcClient = nettyRpcClient;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast(new LengthFieldBasedFrameDecoder(Message.MAX_LENGTH, 4, 4, -8, 0));
        pipeline.addLast(new RpcMessageCodec());
        pipeline.addLast(new ClientMessageInboundHandler(nettyRpcClient.getWaitingRequestPool()));
    }
}
