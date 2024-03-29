package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.util.HashUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Ketama一致性hash
 * (为每个真实的节点创建多个虚拟节点，让节点在环形上的分布更加均匀，使后续调用更加均匀)
 * 所有hash code 在 0 - 2^32-1 内
 */
public class ConsistentHashLoadBalance implements LoadBalance{
    //<服务名,函数名, Selector>
    private final ConcurrentMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    public ProviderURL select(List<ProviderURL> providers, RpcRequest rpcRequest) {
        ConsistentHashSelector selector = selectors.get(rpcRequest.getServiceName() + rpcRequest.getMethodName());

        if(selector == null || selector.providersHashCode != providers.hashCode()){
            selector = new ConsistentHashSelector(providers);
            selectors.put(rpcRequest.getServiceName(), selector);
        }

        return selector.select(rpcRequest);
    }

    private static class ConsistentHashSelector{
        private final int providersHashCode;                          //用于判断Provider列表的改变
        private final TreeMap<Long, ProviderURL> virtualNodes = new TreeMap<>();
        private final int replicaNumber = 160;                        //虚拟节点数量

        public ConsistentHashSelector(List<ProviderURL> providers){
            this.providersHashCode = providers.hashCode();

            //生成虚拟节点，分布到环上
            for (ProviderURL provider : providers) {
                for(int i = 0; i < replicaNumber/4; i++){
                    byte[] md5 = HashUtil.getMD5(provider.toString() + '#' + i);
                    //切成4个使用
                    for(int j = 0; j < 4; j++){
                        long hashCode = splitMD5(md5, j*4);
                        virtualNodes.put(hashCode, provider);
                    }
                }
            }
        }

        public ProviderURL select(RpcRequest rpcRequest){
            long requestHashCode = requestHashCode(rpcRequest);
            Map.Entry<Long, ProviderURL> nodeEntry = virtualNodes.ceilingEntry(requestHashCode);
            if(nodeEntry == null){
                nodeEntry = virtualNodes.firstEntry();
            }
            return nodeEntry.getValue();
        }

        private long requestHashCode(RpcRequest rpcRequest){
            return Arrays.hashCode(rpcRequest.getParams());
        }

        private long splitMD5(byte[] md5, int begin){
            return ((md5[begin] & 0xFFL) << 24) | ((md5[begin + 1] & 0xFFL) << 16) | ((md5[begin + 2] & 0xFFL) << 8) | (md5[begin + 3] & 0xFF);
        }
    }
}
