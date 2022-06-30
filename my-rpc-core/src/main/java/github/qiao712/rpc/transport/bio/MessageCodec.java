package github.qiao712.rpc.transport.bio;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.proto.Message;
import github.qiao712.rpc.proto.MessageType;
import github.qiao712.rpc.proto.SerializationType;
import github.qiao712.rpc.serializer.Serializer;

import java.io.*;

/**
 * 适用于BIO实现的RpcClient,RpcServer 的消息编解码器
 * 消息 <---> 流 或 IO
 */
public class MessageCodec {
    public byte[] encodeMessage(Message<?> message){
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            encodeMessage(message, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RpcException("消息编码错误", e);
        }
    }

    public void encodeMessage(Message<?> message, OutputStream outputStream){
        //检查字段完整性
        if(message.getMessageType() == null){
            throw new RpcException("消息类型未知");
        }
        if(message.getSerializationType() == null){
            throw new RpcException("序列化方式未知");
        }
        if(message.getPayload() == null){
            throw new RpcException("无有效负载");
        }

        //使用对应序列化器对负载的对象进行序列化
        Serializer serializer = message.getSerializationType().getSerializer();
        byte[] payload = serializer.serialize(message.getPayload());
        message.setLength(payload.length + Message.HEADER_LENGTH);

        //限制长度
        if(message.getLength() > Message.MAX_LENGTH){
            throw new RpcException("有效负载过长");
        }

        try{
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeInt(Message.MAGIC_NUMBER);
            dataOutputStream.writeInt(message.getLength());
            dataOutputStream.writeInt(message.getMessageType().ordinal());
            dataOutputStream.writeInt(message.getSerializationType().ordinal());
            dataOutputStream.write(payload);
        } catch (IOException e) {
            throw new RpcException("消息编码错误", e);
        }
    }

    public Message<Object> decodeMessage(byte[] data){
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data)){
            return decodeMessage(byteArrayInputStream);
        } catch (IOException e) {
            throw new RpcException("消息解码错误", e);
        }

    }

    public Message<Object> decodeMessage(InputStream inputStream){
        Message<Object> message = new Message<>();

        try{
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            //magic number
            int magicNumber = dataInputStream.readInt();
            if(magicNumber != Message.MAGIC_NUMBER){
                throw new RpcException("消息格式错误");
            }

            //length
            message.setLength(dataInputStream.readInt());
            if(message.getLength() > Message.MAX_LENGTH || message.getLength() < Message.HEADER_LENGTH){
                throw new RpcException("无效的消息长度");
            }

            //message type
            int messageTypeCode = dataInputStream.readInt();
            if(MessageType.values().length <= messageTypeCode || messageTypeCode < 0){
                throw new RpcException("不存在消息类型:" + messageTypeCode);
            }
            message.setMessageType(MessageType.values()[messageTypeCode]);

            //serialization type
            int serializationTypeCode = dataInputStream.readInt();
            if(SerializationType.values().length <= serializationTypeCode || serializationTypeCode < 0){
                throw new RpcException("不存在消息序列化方式:" + serializationTypeCode);
            }
            message.setSerializationType(SerializationType.values()[serializationTypeCode]);

            //payload
            int payloadLength = message.getLength() - Message.HEADER_LENGTH;
            byte[] payload = new byte[payloadLength];
            for(int offset = 0; offset < payloadLength; ){
                int l = inputStream.read(payload, offset, payloadLength - offset);
                if(l == -1){
                    throw new RpcException("消息读取中断");
                }else{
                    offset += l;
                }
            }
            Serializer serializer = message.getSerializationType().getSerializer();
            Object payloadObject = serializer.deserialize(payload, message.getMessageType().getPayloadClass());
            message.setPayload(payloadObject);

            return message;
        } catch (IOException e) {
            throw new RpcException("消息解码错误", e);
        }
    }
}
