package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ProviderURL;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 按权重轮询
 */
public class RoundRobinLoadBalance implements LoadBalance{
    //节点的当前权重信息
    private static class RoundRobinNode {
        int weight;
        AtomicLong current = new AtomicLong(0);     //当前值
        long lastUpdateTime;                                  //上次更新current值的时间（即上次被使用的时间）

        RoundRobinNode(int weight) {
            this.weight = weight;
        }

        //current += weight
        long increaseCurrent(){
            return current.addAndGet(weight);
        }

        void subtractTotal(long total){
            current.addAndGet(-total);
        }
    }

    //为每个服务名.方法名，隔离权重的上下文信息<服务名.方法名, <提供者地址, 权重信息>>
    ConcurrentMap<String, ConcurrentMap<InetSocketAddress, RoundRobinNode>> weightContext = new ConcurrentHashMap<>();

    //一个上下文中，一个提供者的权重信息节点若在该时间内没有被使用，则清除
    private static final int RECYCLE_PERIOD = 60000;

    @Override
    public ProviderURL select(List<ProviderURL> providers, RpcRequest rpcRequest) {
        //服务于对该方法调用的上下文信息
        String key = rpcRequest.getServiceName() + "." + rpcRequest.getMethodName();
        ConcurrentMap<InetSocketAddress, RoundRobinNode> context = weightContext.computeIfAbsent(key, k->new ConcurrentHashMap<>());

        long totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        RoundRobinNode selectedNode = null;
        ProviderURL selectedProvider = null;
        long now = System.currentTimeMillis();

        for (ProviderURL provider : providers) {
            //获取或初始化该节点的权重信息
            RoundRobinNode node = context.computeIfAbsent(provider.getAddress(), k -> new RoundRobinNode(Math.max(1,provider.getWeight())));

            //更新权重
            if(node.weight != provider.getWeight()){
                node.weight = provider.getWeight();
            }

            //更新当前值 current += weight
            long current = node.increaseCurrent();
            node.lastUpdateTime = now;

            totalWeight += current;

            if(current > maxCurrent){
                maxCurrent = current;
                selectedNode = node;
                selectedProvider = provider;
            }
        }

        //上下文中记录了不存在的节点的当前权重信息，通过时间判断进行删除
        if(providers.size() < context.size()){
            context.entrySet().removeIf(e->e.getValue().lastUpdateTime < now - RECYCLE_PERIOD);
        }

        if(selectedNode != null){
            selectedNode.subtractTotal(totalWeight);
            return selectedProvider;
        }

        return providers.get(0);
    }
}
