package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ProviderURL;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 按权重随机
 */
public class RandomLoadBalance implements LoadBalance{
    //缓存的权重信息 <服务名, 权重信息>
    private final ConcurrentMap<String, WeightCache> cachedWeights = new ConcurrentHashMap<>();
    private static class WeightCache {
        int totalWeight;                //总权重
        boolean same;                   //是否所有provider的权重都相等
        List<ProviderURL> providers;    //用于判断provider列表是否失效

        public WeightCache(int totalWeight, boolean same, List<ProviderURL> providers) {
            this.totalWeight = totalWeight;
            this.same = same;
            this.providers = providers;
        }
    }

    @Override
    public ProviderURL select(List<ProviderURL> providers, RpcRequest rpcRequest) {
        //从缓存中取得 或 重新生成
        WeightCache weightCache = cachedWeights.compute(rpcRequest.getServiceName(), (k, v)->{
            //若providers改变则重新生成
            return (v == null || v.providers != providers) ? generateWeightCache(providers) : v;
        });

        if(weightCache.totalWeight > 0 && !weightCache.same){
            int offset = ThreadLocalRandom.current().nextInt(weightCache.totalWeight);
            for (ProviderURL provider : providers) {
                if(offset < provider.getWeight()){
                    return provider;
                }
                offset -= provider.getWeight();
            }
        }

        //若都相等（都为0）
        return providers.get(ThreadLocalRandom.current().nextInt(providers.size()));
    }

    private WeightCache generateWeightCache(List<ProviderURL> providers){
        int totalWeight = 0;
        int lastWeight = providers.get(0).getWeight();
        boolean same = true;    //是否所有权重都相同
        for (ProviderURL provider : providers) {
            totalWeight += provider.getWeight();

            if(same && lastWeight != provider.getWeight()){
                same = false;
            }
            lastWeight = provider.getWeight();
        }
        return new WeightCache(totalWeight, same, providers);
    }
}