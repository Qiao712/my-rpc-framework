package github.qiao712;

import github.qiao712.rpc.cluster.Cluster;
import github.qiao712.rpc.cluster.FailfastCluster;
import github.qiao712.rpc.cluster.FailoverCluster;
import github.qiao712.rpc.cluster.FailsafeCluster;
import github.qiao712.rpc.loadbalance.ConsistentHashLoadBalance;
import github.qiao712.rpc.proto.SerializationType;
import github.qiao712.rpc.proxy.JDKRpcProxyFactory;
import github.qiao712.rpc.proxy.RpcProxyFactory;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;
import github.qiao712.rpc.transport.netty.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import qiao712.domain.Hello;
import qiao712.service.TestService;

import java.net.InetSocketAddress;

@Slf4j
public class TestNettyConsumer {
    public static void main(String[] args) {
        RpcClient rpcClient = new NettyRpcClient("127.0.0.1", 10056);
        rpcClient.setSerializationType(SerializationType.HESSIAN_SERIALIZATION);
        rpcClient.setResponseTimeout(1000L);

        ServiceDiscovery serviceDiscovery = new ZookeeperServiceDiscovery(new InetSocketAddress("8.141.151.176", 2181));

//        Cluster cluster = new FailfastCluster(rpcClient, serviceDiscovery, new ConsistentHashLoadBalance());
//        FailoverCluster cluster = new FailoverCluster(rpcClient, serviceDiscovery, new ConsistentHashLoadBalance());
//        cluster.setRetries(10);
//        Cluster cluster = new FailsafeCluster(rpcClient, serviceDiscovery, new ConsistentHashLoadBalance());
        Cluster cluster = new FailfastCluster(rpcClient, serviceDiscovery, new ConsistentHashLoadBalance());

        RpcProxyFactory rpcProxyFactory = new JDKRpcProxyFactory();

        //订阅服务
        serviceDiscovery.subscribeService(TestService.class.getCanonicalName());

        //创建一个桩对象进行调用
        TestService testService = rpcProxyFactory.createProxy(TestService.class, TestService.class.getCanonicalName(), cluster);
//        System.out.println(testService.add(123, 123));
//        System.out.println(testService.add(123,123,123));
        testService.delay(5000);
//        System.out.println(testService.hello(new Hello(23, "hello")));
//        testService.testThrow();

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
