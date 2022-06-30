package github.qiao712.test;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import github.qiao712.proto.*;
import github.qiao712.rpc.proto.*;
import github.qiao712.rpc.serializer.JDKSerializer;
import github.qiao712.rpc.serializer.Serializer;
import github.qiao712.rpc.transport.bio.MessageCodec;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
        MessageCodec messageCodec = new MessageCodec();

        RpcRequest rpcRequest = new RpcRequest("testService", "testMethod", new Object[0]);
        Message<RpcRequest> message = new Message<>();
        message.setPayload(rpcRequest);
        message.setMessageType(MessageType.REQUEST);
        message.setSerializationType(SerializationType.JDK_SERIALIZATION);

        byte[] data = messageCodec.encodeMessage(message);

        //检查encodeMessage置入的长度字段是否正确
        assert data.length == message.getLength();

        Message<Object> originMessage = messageCodec.decodeMessage(data);

        System.out.println(originMessage);
    }




    static class TestDto2{

    }

//    @Test
//    public void testKryo(){
//        Kryo kryo = new Kryo();
//        kryo.register(TestDto.class);
//        kryo.register(TestDto2.class);
//
//        Output output = new Output(10240);
//        kryo.writeObject(output, new TestDto("xxx", 123));
//        output.flush();
//        byte[] data = output.getBuffer();
//
//        Kryo kryo1 = new Kryo();
//        kryo1.register(TestDto2.class);
//        kryo1.register(TestDto.class);
//        Input input = new Input(data);
//        TestDto testDto = kryo1.readObject(input, TestDto.class);
//
//        System.out.println(testDto);
//    }
//
//    @Test
//    public void testKryo2(){
//        Kryo kryo = new Kryo();
//        kryo.register(RpcRequest.class);
//        kryo.register(RpcResponse.class);
//        kryo.register(Class.class);
//
//        RpcResponse rpcResponse = new RpcResponse();
//        rpcResponse.setCode(RpcResponseCode.SUCCESS);
//        rpcResponse.setData(new TestDto("xxx", 123));
//
//        Output output = new Output(10240);
//        kryo.writeClassAndObject(output, RpcResponse.class);
//        byte[] data = output.getBuffer();
//
//        Input input = new Input(data);
//        RpcResponse rpcResponse1 = kryo.readObject(input, RpcResponse.class);
//        System.out.println(rpcResponse1);
//        System.out.println(rpcResponse1.getData().getClass());
//    }

    @Test
    public void testHessian() throws IOException {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(RpcResponseCode.SUCCESS);
        rpcResponse.setData(new TestDto("xxx", 123));


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArrayOutputStream);
        output.writeObject(rpcResponse);
        output.flush();

        byte[] data = byteArrayOutputStream.toByteArray();
        System.out.println(data.length);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        Hessian2Input input = new Hessian2Input(byteArrayInputStream);
        RpcResponse rpcResponse1 = (RpcResponse) input.readObject(RpcResponse.class);
        System.out.println(rpcResponse1);
    }

    @Test
    public void testHessianOutput() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArrayOutputStream);
        output.close();

        byteArrayOutputStream.write(123);
    }
}
