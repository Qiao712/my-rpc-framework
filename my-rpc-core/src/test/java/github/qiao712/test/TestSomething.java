package github.qiao712.test;

import github.qiao712.proto.*;
import github.qiao712.serializer.JDKSerializer;
import github.qiao712.serializer.Serializer;
import org.junit.Test;

import java.awt.*;

public class TestSomething {
    @Test
    public void testSerializer(){
        Serializer serializer = new JDKSerializer();

        String test = "ssss";
        byte[] data = serializer.serialize(test);
        String test2 = serializer.deserialize(data, String.class);
        System.out.println(test2);
    }

    @Test
    public void testMessageCoder(){
        MessageCoder messageCoder = new MessageCoder();

        RpcRequest rpcRequest = new RpcRequest("testService", "testMethod", new Object[0]);
        Message<RpcRequest> message = new Message<>();
        message.setPayload(rpcRequest);
        message.setMessageType(MessageType.REQUEST);
        message.setSerializationType(SerializationType.JDK_SERIALIZATION);

        byte[] data = messageCoder.encodeMessage(message);

        //检查encodeMessage置入的长度字段是否正确
        assert data.length == message.getLength();

        Message<Object> originMessage = messageCoder.decodeMessage(data);

        System.out.println(originMessage);
    }
}
