package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ProviderURL;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 按权重随机
 */
public class RandomLoadBalance implements LoadBalance{
    @Override
    public ProviderURL select(List<ProviderURL> providers, RpcRequest rpcRequest) {
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

        if(totalWeight > 0 && !same){
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            for (ProviderURL provider : providers) {
                if(offset < provider.getWeight()){
                    return provider;
                }
                offset -= provider.getWeight();;
            }
        }

        //若都相等（都为0）
        return providers.get(ThreadLocalRandom.current().nextInt(providers.size()));
    }
}