package github.qiao712.rpc.transport.netty.client;

import github.qiao712.rpc.proto.Message;
import github.qiao712.rpc.proto.MessageType;
import github.qiao712.rpc.proto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 处理消费者一侧的入站消息
 */
@Slf4j
public class ClientMessageInboundHandler extends SimpleChannelInboundHandler<Message<?>> {
    private final WaitingRequestPool waitingRequestPool;
    private final ChannelProvider channelProvider;

    //记录有几个心跳间隔没有受到回应
    private final AtomicInteger idleCount = new AtomicInteger(0);

    public ClientMessageInboundHandler(WaitingRequestPool waitingRequestPool, ChannelProvider channelProvider) {
        this.waitingRequestPool = waitingRequestPool;
        this.channelProvider = channelProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message<?> message) throws Exception {
        idleCount.set(0);

        switch(message.getMessageType()){
            case RESPONSE:{
                //完成所匹配的请求
                waitingRequestPool.completeRequest(message.getRequestId(), (RpcResponse) message.getPayload());
                log.debug("响应: {}", message.getPayload());
                break;
            }

            case PONG:{
                log.debug("来自{}的心跳响应", ctx.channel().remoteAddress());
                break;
            }

            default:{
                log.error("消费者接收到不支持的类型的消息(MessageType = {})", message.getMessageType());
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        channelProvider.removeChannel(socketAddress);
        log.debug("链接({})已断开.", ctx.channel().remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 读空闲 超过心跳间隔发送心跳
        if (evt instanceof IdleStateEvent && ((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
            if(idleCount.incrementAndGet() > 3){
                //超过3个间隔没有受到回应，认为对方下线，移除当前连接，并定时尝试重连
                channelProvider.removeChannel(ctx.channel());
                channelProvider.tryReconnect(ctx.channel().remoteAddress());

                log.info("{}无响应,已移除连接.", ctx.channel().remoteAddress());
                return;
            }

            //发送心跳
            Message<Void> message =new Message<>();
            message.setMessageType(MessageType.PING);
            ctx.writeAndFlush(message);
            log.debug("向{}发送心跳.", ctx.channel().remoteAddress());
        }
    }
}
