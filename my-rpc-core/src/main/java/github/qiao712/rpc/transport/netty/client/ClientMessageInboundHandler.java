package github.qiao712.rpc.transport.netty.client;

import github.qiao712.rpc.proto.Message;
import github.qiao712.rpc.proto.MessageType;
import github.qiao712.rpc.proto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理消费者一侧的入站消息
 */
@Slf4j
public class ClientMessageInboundHandler extends SimpleChannelInboundHandler<Message<?>> {
    private final WaitingRequestPool waitingRequestPool;

    public ClientMessageInboundHandler(WaitingRequestPool waitingRequestPool) {
        this.waitingRequestPool = waitingRequestPool;
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
}
