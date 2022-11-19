package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ProviderURL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 按权重轮询
 */
public class RoundRobinLoadBalance implements LoadBalance{
    //缓存的权重信息 <服务名, 权重信息>
    private final ConcurrentMap<String, WeightCache> cachedWeightInfo = new ConcurrentHashMap<>();
    private static class Node{
        ProviderURL providerURL;
        AtomicLong current = new AtomicLong(0); //当前值

        //current += weight
        long increaseCurrent(){
            return current.addAndGet(providerURL.getWeight());
        }
    }
    private static class WeightCache {
        List<Node> nodes;
        List<ProviderURL> providers;    //用于判断provider列表是否失效
    }

    @Override
    public ProviderURL select(List<ProviderURL> providers, RpcRequest rpcRequest) {
        //从缓存中取得 或 重新生成
        WeightCache weightCache = cachedWeightInfo.compute(rpcRequest.getServiceName(), (k, v)->{
            //若providers改变则重新生成
            return (v == null || v.providers != providers) ? generateWeightCache(providers) : v;
        });

        long totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        Node selected = null;
        for (Node node : weightCache.nodes) {
            //更新当前值 current += weight
            long current = node.increaseCurrent();

            //找到当前值最大的
            if(current > maxCurrent){
                maxCurrent = current;
                selected = node;
            }

            totalWeight += current;
        }

        if(selected != null){
            selected.current.addAndGet(-totalWeight);
            return selected.providerURL;
        }

        return providers.get(0);
    }

    private WeightCache generateWeightCache(List<ProviderURL> providers){
        WeightCache weightCache = new WeightCache();
        weightCache.providers = providers;
        weightCache.nodes = new ArrayList<>(providers.size());
        for (ProviderURL provider : providers) {
            Node node = new Node();
            node.providerURL = provider;
            weightCache.nodes.add(node);
        }
        return weightCache;
    }
}
