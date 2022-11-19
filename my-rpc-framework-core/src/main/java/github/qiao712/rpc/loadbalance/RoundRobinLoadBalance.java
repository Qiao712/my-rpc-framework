package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ProviderURL;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 按权重轮询
 */
public class RoundRobinLoadBalance implements LoadBalance{
    private static class WeightedRoundRobin{
        int weight;
        AtomicLong current = new AtomicLong(0);
    }

    //缓存每个节点的权重
    private final ConcurrentMap<ProviderURL, WeightedRoundRobin> weightMap = new ConcurrentHashMap<>();

    @Override
    public ProviderURL select(List<ProviderURL> providers, RpcRequest rpcRequest) {
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        ProviderURL selectedProvider = null;
        WeightedRoundRobin selectedWrr = null;

        for (ProviderURL provider : providers) {
            //添加新节点的权重信息
            WeightedRoundRobin wrr = weightMap.computeIfAbsent(provider, k -> {
                WeightedRoundRobin newWrr = new WeightedRoundRobin();
                newWrr.weight = k.getWeight();
                return newWrr;
            });

            //更新权重信息
            if(Objects.equals(provider.getWeight(), wrr.weight)){
                wrr.weight = provider.getWeight();
            }

            totalWeight += provider.getWeight();

            //current += weight
            long current = wrr.current.addAndGet(wrr.weight);
            if(current > maxCurrent){
                maxCurrent = current;
                selectedProvider = provider;
                selectedWrr = wrr;
            }
        }

        //新的节点在之前的循环中被添加了， weightMap的长度一定不小于providers的长度
        //若长度不一致说providers中有节点下线了，需刷新weightMap
        if(weightMap.size() != providers.size()){
            weightMap.clear();
        }

        if(selectedProvider != null){
            selectedWrr.current.addAndGet(-totalWeight);
            return selectedProvider;
        }

        return providers.get(0);
    }
}
