package github.qiao712.test;

import github.qiao712.rpc.loadbalance.ConsistentHashLoadBalance;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.*;

public class TestLoadBalance {
    @Test
    public void testConcurrentHashLoadBalance(){
        LoadBalance loadBalance = new ConsistentHashLoadBalance();
        Random random = new Random();

        //服务提供者地址列表
        List<InetSocketAddress> addresses = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            addresses.add(new InetSocketAddress("xxxx", random.nextInt(65536)));
        }

        //测试不同参数
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("testService");
        Map<InetSocketAddress, Integer> count = new HashMap<>();
        for(int i = 0; i < 10000; i++){
            rpcRequest.setArgs(new Object[]{random.nextInt()});
            InetSocketAddress selected = loadBalance.select(addresses, rpcRequest);
            count.compute(selected, (key, i1) -> i1 == null ? 1 : i1 + 1);
        }

        int sum = 0;
        for (Integer value : count.values()) {
            System.out.print(value + " ");
            sum += value;
        }
        System.out.println();
        System.out.println("使用的节点数:" + count.size());

        //一个服务下线
        addresses.remove(0);
        count = new HashMap<>();
        for(int i = 0; i < 10000; i++){
            rpcRequest.setArgs(new Object[]{random.nextInt()});
            InetSocketAddress selected = loadBalance.select(addresses, rpcRequest);
            count.compute(selected, (key, i1) -> i1 == null ? 1 : i1 + 1);
        }
        for (Integer value : count.values()) {
            System.out.print(value + " ");
            sum += value;
        }
        System.out.println();
        System.out.println("使用的节点数:" + count.size());

        //一个服务实例上线
        addresses.add(new InetSocketAddress("new", 4444));
        for(int i = 0; i < 10000; i++){
            rpcRequest.setArgs(new Object[]{random.nextInt()});
            InetSocketAddress selected = loadBalance.select(addresses, rpcRequest);
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
