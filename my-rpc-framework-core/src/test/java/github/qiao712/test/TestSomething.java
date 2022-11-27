package github.qiao712.test;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import github.qiao712.rpc.proto.*;
import github.qiao712.rpc.serializer.JDKSerializer;
import github.qiao712.rpc.serializer.Serializer;
import github.qiao712.rpc.transport.bio.RpcMessageCodec;
import github.qiao712.rpc.util.AutoIncrementIdGenerator;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
        RpcMessageCodec rpcMessageCodec = new RpcMessageCodec();

        RpcRequest rpcRequest = new RpcRequest("testService", "testMethod", new Object[0], null);
        Message<RpcRequest> message = new Message<>();
        message.setPayload(rpcRequest);
        message.setMessageType(MessageType.REQUEST);
        message.setSerializationType(SerializationType.JDK_SERIALIZATION);

        byte[] data = rpcMessageCodec.encodeMessage(message);

        //检查encodeMessage置入的长度字段是否正确
        assert data.length == message.getLength();

        Message<Object> originMessage = rpcMessageCodec.decodeMessage(data);

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

    /**
     * 测试线程安全的自增ID
     */
    @Test
    public void testIdGenerator(){
        int threadNum = 5;
        int n = 10;

        AutoIncrementIdGenerator autoIncrementIdGenerator = AutoIncrementIdGenerator.getInstance();
        boolean[] flag = new boolean[threadNum * n];
        Thread[] threads = new Thread[threadNum];

        for(int i = 0; i < threadNum; i++){
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < n; i++){
                        int id = autoIncrementIdGenerator.generateId();
                        flag[id] = true;
                        System.out.println(Thread.currentThread() + " : "+ id);
                    }
                }
            });

            threads[i].start();
        }

        for(int i = 0; i < threadNum; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //检查[0, threadNum * n]是否都被覆盖
        for(int i = 0; i < threadNum * n; i++){
            if(! flag[i]){
                System.out.println("!!!!!!");
            }
        }
    }

    @Test
    public void testCompletableFuture() throws ExecutionException, InterruptedException {
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.complete(123);
        completableFuture.complete(23);
        System.out.println(completableFuture.get());
    }
}
