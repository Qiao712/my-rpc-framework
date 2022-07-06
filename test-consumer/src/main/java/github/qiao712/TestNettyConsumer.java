package github.qiao712;

import com.caucho.hessian.test.Test;
import github.qiao712.rpc.proto.SerializationType;
import github.qiao712.rpc.proxy.DefaultInvoker;
import github.qiao712.rpc.proxy.Invoker;
import github.qiao712.rpc.proxy.JDKRpcProxyFactory;
import github.qiao712.rpc.proxy.RpcProxyFactory;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;
import github.qiao712.rpc.transport.netty.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;
import qiao712.domain.Hello;
import qiao712.service.TestService;

import java.net.InetSocketAddress;

@Slf4j
public class TestNettyConsumer {
    public static void main(String[] args) {
        RpcClient rpcClient = new NettyRpcClient("127.0.0.1", 9712);
        rpcClient.setSerializationType(SerializationType.HESSIAN_SERIALIZATION);
        rpcClient.setResponseTimeout(1000L);

        ServiceDiscovery serviceDiscovery = new ZookeeperServiceDiscovery(new InetSocketAddress("8.141.151.176", 2181));

        Invoker invoker = new DefaultInvoker(rpcClient, serviceDiscovery);

        RpcProxyFactory rpcProxyFactory = new JDKRpcProxyFactory(invoker);

        //订阅服务
        serviceDiscovery.subscribeService(TestService.class.getCanonicalName());

        //创建一个桩对象进行调用
        TestService testService = rpcProxyFactory.createProxy(TestService.class);
        System.out.println(testService.add(123, 123));
        System.out.println(testService.add(123,123,123));
        testService.delay(3000);
        System.out.println(testService.hello(new Hello(23, "hello")));
        testService.testThrow();

//        long n = 1000;
//        long begin = System.nanoTime();
//        for(int i = 0; i < n; i++){
//            testService.add(231, 123);
//        }
//        long end = System.nanoTime();
//
//        log.debug("{}次调用耗时{}ms", n, (end-begin)/1000000.0);
    }
}
