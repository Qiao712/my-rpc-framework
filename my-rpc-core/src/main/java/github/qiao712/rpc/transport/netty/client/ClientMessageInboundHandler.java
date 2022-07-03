package github.qiao712.rpc.transport.netty.client;

import github.qiao712.rpc.proto.Message;
import github.qiao712.rpc.proto.MessageType;
import github.qiao712.rpc.proto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 处理消费者一侧的入站消息
 */
@Slf4j
public class ClientMessageInboundHandler extends SimpleChannelInboundHandler<Message<?>> {
    private final WaitingRequestPool waitingRequestPool;
    private final ClientChannelPool clientChannelPool;

    public ClientMessageInboundHandler(WaitingRequestPool waitingRequestPool, ClientChannelPool clientChannelPool) {
        this.waitingRequestPool = waitingRequestPool;
        this.clientChannelPool = clientChannelPool;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message<?> message) throws Exception {
        if(message.getMessageType() == MessageType.RESPONSE){
            log.debug("响应: {}", message.getPayload());

            //完成所匹配的请求
            waitingRequestPool.completeRequest(message.getRequestId(), (RpcResponse) message.getPayload());
        }else{
            log.error("消费者接收到不支持的类型的消息(MessageType = {})", message.getMessageType());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        clientChannelPool.removeChannel(socketAddress);
        log.debug("链接({})已断开", ctx.channel().remoteAddress());
    }
}
