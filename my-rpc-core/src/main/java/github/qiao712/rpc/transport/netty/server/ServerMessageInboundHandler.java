package github.qiao712.rpc.transport.netty.server;


import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.proto.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerMessageInboundHandler extends SimpleChannelInboundHandler<Message<?>> {
    private final RequestHandler requestHandler;
    private final SerializationType serializationType;

    public ServerMessageInboundHandler(RequestHandler requestHandler, SerializationType serializationType) {
        this.requestHandler = requestHandler;
        this.serializationType = serializationType;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message<?> message) throws Exception {
        if(message.getMessageType() == MessageType.REQUEST){
            log.debug("请求: {}", message.getPayload());

            //处理请求
            RpcResponse rpcResponse = requestHandler.handleRequest((RpcRequest) message.getPayload());

            //返回请求
            Message<RpcResponse> responseMessage = new Message<>();
            responseMessage.setMessageType(MessageType.RESPONSE);
            responseMessage.setRequestId(message.getRequestId());
            responseMessage.setSerializationType(serializationType);
            responseMessage.setPayload(rpcResponse);
            ctx.writeAndFlush(responseMessage);

            log.debug("响应: {}", rpcResponse);
        }else{
            log.error("消费者接收到不支持的类型的消息(MessageType = {})", message.getMessageType());
        }
    }
}
