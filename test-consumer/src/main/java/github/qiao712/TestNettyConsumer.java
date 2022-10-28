package github.qiao712;

import github.qiao712.rpc.cluster.FailoverCluster;
import github.qiao712.rpc.loadbalance.ConsistentHashLoadBalance;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.SerializationType;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestNettyConsumer {
    public static TestService testService;

    public static void main(String[] args) {
        //创建客户端并进行设置
        RpcClient rpcClient = new NettyRpcClient();
        rpcClient.setSerializationType(SerializationType.HESSIAN_SERIALIZATION);
        rpcClient.setResponseTimeout(1000L);

        //服务发现组件
        ServiceDiscovery serviceDiscovery = new ZookeeperServiceDiscovery(new InetSocketAddress("114.116.245.83", 2181));

        //选择负载均衡策略
        LoadBalance loadBalance = new ConsistentHashLoadBalance();

        //创建Cluster，整合RpcClient, ServiceDiscovery, LoadBalance
        FailoverCluster cluster = new FailoverCluster(rpcClient, serviceDiscovery, loadBalance);
        cluster.setRetries(100);

        //订阅服务
        serviceDiscovery.subscribeService(TestService.class.getCanonicalName());

        //创建一个桩对象进行调用
        RpcProxyFactory rpcProxyFactory = new JDKRpcProxyFactory();
        testService = rpcProxyFactory.createProxy(TestService.class, TestService.class.getCanonicalName(), cluster);

        //线程池处理请求：2705.1489ms 2291.5871ms 2314.673ms
        //2578.6105 2219.8362

        //
        // 3033 2933
        testMultiThread();
    }

    public static void tryOnce(){
        //调用测
        System.out.println(testService.add(123, 123));
        System.out.println(testService.add(123,123,123));
        testService.delay(5000);
        System.out.println(testService.hello(new Hello(23, "hello")));
        testService.testThrow();
    }

    public static void testSingleThread(){
        long n = 1000 * Runtime.getRuntime().availableProcessors();
        long fail = 0;
        long success = 0;
        long begin = System.nanoTime();
        for(int i = 0; i < n; i++){
            try{
                testService.add(i, i);
                success++;
            }catch (Throwable throwable){
                log.error("失败", throwable);
                fail++;
            }
        }
        long end = System.nanoTime();

        System.out.println("失败次数:" + fail);
        System.out.println("成功次数:" + success);
        System.out.println("耗时(ms):" + (end-begin)/1000000.0);
    }

    public static void testMultiThread(){
        int threadNum = Runtime.getRuntime().availableProcessors() * 2;
        int n = 1000;


        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        long begin = System.nanoTime();
        for(int j = 0; j < threadNum; j++){
            executorService.execute(()->{
                for(int i = 0; i < n; i++){
                    testService.add(i, i);
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(1000, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.nanoTime();

        System.out.println("耗时(ms):" + (end-begin)/1000000.0);
    }
}
