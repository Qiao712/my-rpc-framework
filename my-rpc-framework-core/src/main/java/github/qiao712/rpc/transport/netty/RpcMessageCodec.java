package github.qiao712.rpc.transport.netty;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.proto.Message;
import github.qiao712.rpc.proto.MessageType;
import github.qiao712.rpc.proto.SerializationType;
import github.qiao712.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RpcMessageCodec extends ByteToMessageCodec<Message<?>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message<?> message, ByteBuf out) throws Exception {
        //检查合法性
        message.check();

        //magic number
        out.writeInt(Message.MAGIC_NUMBER);

        //length 占位，序列化后才能得知总长度
        out.writeInt(0);

        //requestId
        out.writeInt(message.getRequestId());

        //message type
        out.writeInt(message.getMessageType().ordinal());

        //serialization type
        out.writeInt(message.getSerializationType().ordinal());

        //输出序列化的负载对象
        if(message.getPayload() != null){
            ByteBufOutputStream byteBufOutputStream = new ByteBufOutputStream(out);
            Serializer serializer = message.getSerializationType().getSerializer();
            serializer.serialize(message.getPayload(), byteBufOutputStream);
        }

        //回去写length
        int length = out.readableBytes();
        out.markWriterIndex();
        out.writerIndex(4);
        out.writeInt(length);
        out.resetWriterIndex();

        log.debug("编码消息: {}", message);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNumber = in.readInt();
        if(magicNumber != Message.MAGIC_NUMBER){
            throw new RpcException("错误的协议");
        }

        Message<Object> message = new Message<>();

        //length
        message.setLength(in.readInt());
        if(message.getLength() > Message.MAX_LENGTH || message.getLength() < Message.HEADER_LENGTH){
            throw new RpcException("无效的消息长度");
        }

        //requestId
        message.setRequestId(in.readInt());

        //message type
        int messageTypeCode = in.readInt();
        if(MessageType.values().length <= messageTypeCode || messageTypeCode < 0){
            throw new RpcException("不存在消息类型:" + messageTypeCode);
        }
        message.setMessageType(MessageType.values()[messageTypeCode]);

        //serialization type
        int serializationTypeCode = in.readInt();
        if(SerializationType.values().length <= serializationTypeCode || serializationTypeCode < 0){
            throw new RpcException("不存在消息序列化方式:" + serializationTypeCode);
        }
        message.setSerializationType(SerializationType.values()[serializationTypeCode]);

        //从ByteBuf创建读入流，将有效负载反序列为对象
        if(in.readableBytes() > 0){
            ByteBufInputStream byteBufInputStream = new ByteBufInputStream(in);
            Serializer serializer = message.getSerializationType().getSerializer();
            Object payloadObject = serializer.deserialize(byteBufInputStream, message.getMessageType().getPayloadClass());
            message.setPayload(payloadObject);
        }

        //检查合法性
        message.check();

        out.add(message);

        log.debug("解码消息: {}", message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("ChannelHandler:", cause);
    }
}
