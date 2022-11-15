package github.qiao712.rpc.transport.netty.server;


import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.handler.ResponseSender;
import github.qiao712.rpc.proto.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
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
        switch (message.getMessageType()){
            case REQUEST:{
                log.debug("请求: {}", message.getPayload());

                //用于发送请求的回调
                ResponseSender responseSender = new ResponseSender() {
                    @Override
                    public void send(RpcResponse rpcResponse) {
                        //返回请求
                        Message<RpcResponse> responseMessage = new Message<>();
                        responseMessage.setMessageType(MessageType.RESPONSE);
                        responseMessage.setRequestId(message.getRequestId());
                        responseMessage.setSerializationType(serializationType);
                        responseMessage.setPayload(rpcResponse);
                        ctx.writeAndFlush(responseMessage);
                        log.debug("响应: {}", rpcResponse);
                    }
                };

                requestHandler.handleRequest((RpcRequest) message.getPayload(), responseSender);
                break;
            }

            case PING:{
                //回应心跳
                Message<Void> heartbeatAck = new Message<>();
                heartbeatAck.setMessageType(MessageType.PONG);
                ctx.writeAndFlush(heartbeatAck);
                break;
            }

            default: {
                log.error("消费者接收到不支持的类型的消息(MessageType = {})", message.getMessageType());
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if(idleStateEvent.state() == IdleState.ALL_IDLE){
                //清理空闲连接
                ctx.channel().close().sync();
                log.debug("清理空闲链接{}", ctx.channel().remoteAddress());
            }
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }
}
