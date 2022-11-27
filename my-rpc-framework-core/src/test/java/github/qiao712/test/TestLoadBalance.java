package github.qiao712.test;

import github.qiao712.rpc.loadbalance.ConsistentHashLoadBalance;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.loadbalance.RandomLoadBalance;
import github.qiao712.rpc.loadbalance.RoundRobinLoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ProviderURL;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestLoadBalance {
    @Test
    public void testConcurrentHashLoadBalance(){
        LoadBalance loadBalance = new ConsistentHashLoadBalance();
        Random random = new Random();

        //服务提供者地址列表
        List<ProviderURL> providers = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            ProviderURL provider = new ProviderURL();
            provider.setService("testService");
            provider.setWeight(random.nextInt(100));
            provider.setAddress(new InetSocketAddress("xxxx", random.nextInt(65536)));
            providers.add(provider);
        }

        //测试不同参数
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("testService");
        Map<ProviderURL, Integer> count = new HashMap<>();
        for(int i = 0; i < 10000; i++){
            rpcRequest.setParams(new Object[]{random.nextInt()});
            ProviderURL selected = loadBalance.select(providers, rpcRequest);
            count.compute(selected, (key, i1) -> i1 == null ? 1 : i1 + 1);
        }

        int sum = 0;
        for (Integer value : count.values()) {
            System.out.print(value + " ");
            sum += value;
        }
        System.out.println();
        System.out.println("使用的节点数:" + count.size());

        //一个服务下线,后再测试
        providers.remove(0);
        providers = new ArrayList<>(providers);     //必须换一个新的列表
        count = new HashMap<>();
        for(int i = 0; i < 10000; i++){
            rpcRequest.setParams(new Object[]{random.nextInt()});
            ProviderURL selected = loadBalance.select(providers, rpcRequest);
            count.compute(selected, (key, i1) -> i1 == null ? 1 : i1 + 1);
        }
        for (Integer value : count.values()) {
            System.out.print(value + " ");
            sum += value;
        }
        System.out.println();
        System.out.println("使用的节点数:" + count.size());

        //一个服务实例上线,再测试
        ProviderURL provider = new ProviderURL();
        provider.setService("testService");
        provider.setWeight(random.nextInt(100));
        provider.setAddress(new InetSocketAddress("xxxx", random.nextInt(65536)));
        providers.add(provider);
        providers = new ArrayList<>(providers); //必须换一个新的列表

        for(int i = 0; i < 10000; i++){
            rpcRequest.setParams(new Object[]{random.nextInt()});
            ProviderURL selected = loadBalance.select(providers, rpcRequest);
            count.compute(selected, (key, i1) -> i1 == null ? 1 : i1 + 1);
        }
        for (Integer value : count.values()) {
            System.out.print(value + " ");
            sum += value;
        }
        System.out.println();
        System.out.println("使用的节点数:" + count.size());
    }

    @Test
    public void testRandomLoadbalance(){
        LoadBalance loadBalance = new RandomLoadBalance();

        //服务提供者地址列表
        Random random = new Random();
        List<ProviderURL> providers = new ArrayList<>();
        Map<ProviderURL, Integer> count = new HashMap<>();
        for(int i = 0; i < 10; i++){
            ProviderURL provider = new ProviderURL();
            provider.setService("testService");
            provider.setWeight(i);  //1 到 10
            provider.setAddress(new InetSocketAddress("xxxx", random.nextInt(65536)));
            providers.add(provider);
        }

        RpcRequest rpcRequest = new RpcRequest();
        for(int i = 0; i < 100000; i++){
            rpcRequest.setServiceName("testService" + random.nextInt(10));
            ProviderURL selected = loadBalance.select(providers, rpcRequest);
            count.compute(selected, (key, i1) -> i1 == null ? 1 : i1 + 1);
        }

        count.forEach((k, v)->{
            System.out.println("权重:" + k.getWeight() + ". 被调用:" + v);
        });
    }

    /**
     * 并发测试 RoundRobin
     */
    @Test
    public void testRoundRobinLoadBalance() throws InterruptedException {
        LoadBalance loadBalance = new RoundRobinLoadBalance();

        //服务提供者地址列表
        Random random = new Random();
        List<ProviderURL> providers = new ArrayList<>();
        Map<ProviderURL, Integer> count = new ConcurrentHashMap<>();
        for(int i = 0; i < 10; i++){
            ProviderURL provider = new ProviderURL();
            provider.setService("testService");
            provider.setWeight(i);  //1 到 10
            provider.setAddress(new InetSocketAddress("xxxx", random.nextInt(65536)));
            providers.add(provider);
        }

        RpcRequest rpcRequest = new RpcRequest();

        int threadN = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadN);
        for(int j = 0; j < threadN; j++){
            executorService.execute(()->{
                for(int i = 0; i < 1000; i++){
                    rpcRequest.setServiceName("testService" + random.nextInt(10));
                    ProviderURL selected = loadBalance.select(providers, rpcRequest);
                    count.compute(selected, (key, i1) -> i1 == null ? 1 : i1 + 1);
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(100, TimeUnit.DAYS);

        count.forEach((k, v)->{
            System.out.println("权重:" + k.getWeight() + ". 被调用:" + v);
        });
    }

    @Test
    public void testByte(){
        byte[] bs = new byte[]{123,45,58,-4};
        for (Byte b : bs) {
            long l = b & 0xFFL;
            System.out.println(Long.toBinaryString(l));
        }

        long l = splitMD5(bs, 0);
        System.out.println(Long.toBinaryString(l));

        long l1 = (bs[0] & 0xFFL) << 24;
        System.out.println(Long.toBinaryString(l1));
    }

    private long splitMD5(byte[] md5, int begin){
        return (md5[begin] & 0xFFL << 24) | (md5[begin + 1] & 0xFFL << 16) | (md5[begin + 2] & 0xFFL << 8) | (md5[begin + 3] & 0xFFL);
    }
}
