package github.qiao712.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import github.qiao712.rpc.proto.*;
import github.qiao712.rpc.serializer.JDKSerializer;
import github.qiao712.rpc.serializer.Serializer;
import github.qiao712.rpc.transport.bio.RpcMessageCodec;
import github.qiao712.rpc.util.AutoIncrementIdGenerator;
import lombok.Data;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestSomething {
    @Test
    public void testSyncSet() throws InterruptedException {
        Set<Integer> set = Collections.synchronizedSet(new HashSet<>());
        set.add(123);

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (Integer integer : set) {
                    System.out.println("for ---");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("-----");
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                set.add(12);
                System.out.println("added");
            }
        });

        thread1.start();
        Thread.sleep(100);
        thread2.start();

        thread1.join();
        thread2.join();
    }

    @Test
    public void testPattern(){
        final Pattern pathPattern = Pattern.compile("/my-rpc/(.*)/providers/.*");
        //节点所在的服务名
        String serviceName = null;
        Matcher matcher = pathPattern.matcher("/my-rpc/test.Service1/providers/123123123");
        if(matcher.find() && matcher.groupCount() == 1){
            serviceName = matcher.group(1);
        }
        System.out.println("service " + serviceName);
    }

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

    @Test
    public void idGenerator(){
        AutoIncrementIdGenerator idGenerator = AutoIncrementIdGenerator.getInstance();

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

    @Data
    static class A{
        int v1;
        String v2;
    }
    @Data
    static class B{
        A a;
        String v3;
        BigDecimal v4;
        LocalDateTime time;
    }
    @Test
    public void testGenericFastJson(){
        A a = new A();
        a.v1 = 1;
        a.v2 = "hello";
        B b = new B();
        b.a = a;
        b.v3 = "world";
        b.v4 = BigDecimal.TEN;
        b.time = LocalDateTime.now();

        //代类型名的JSON
        String s = JSON.toJSONString(b, SerializerFeature.WriteClassName);
        System.out.println(s);

        //自动转换为指定的类型
        Object o = JSON.parseObject(s, Object.class, Feature.SupportAutoType);
        System.out.println(o);
    }
}
