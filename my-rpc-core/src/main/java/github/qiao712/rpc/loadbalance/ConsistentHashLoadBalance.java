package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.util.HashUtil;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Ketama一致性hash
 * (为每个真实的节点创建多个虚拟节点，让节点在环形上的分布更加均匀，使后续调用更加均匀)
 * 所有hash code 在 0 - 2^32-1 内
 */
public class ConsistentHashLoadBalance implements LoadBalance{
    private static class ConsistentHashSelector{
        private final int identityHashCode;                           //用于快速判断提供者列表是否改变
        private final TreeMap<Long, InetSocketAddress> virtualNodes = new TreeMap<>();
        private final int replicaNumber = 160;                  //虚拟节点数量

        public ConsistentHashSelector(List<InetSocketAddress> providers, int identityHashCode){
            this.identityHashCode = identityHashCode;

            //生成虚拟节点，分布到环上
            for (InetSocketAddress provider : providers) {
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

        public int getIdentityHashCode(){
            return identityHashCode;
        }

        public InetSocketAddress select(RpcRequest rpcRequest){
            long requestHashCode = requestHashCode(rpcRequest);
            Map.Entry<Long, InetSocketAddress> nodeEntry = virtualNodes.ceilingEntry(requestHashCode);
            if(nodeEntry != null){
                return nodeEntry.getValue();
            }else if(!virtualNodes.isEmpty()){
                return virtualNodes.firstEntry().getValue();
            }
            return null;
        }

        private long requestHashCode(RpcRequest rpcRequest){
            StringBuilder requestKey = new StringBuilder(rpcRequest.getServiceName());
            Object[] args = rpcRequest.getArgs();
            if(args != null){
                for (Object arg : args) {
                    requestKey.append(arg);
                }
            }

            byte[] md5 = HashUtil.getMD5(requestKey.toString());
            return splitMD5(md5, 0);
        }

        private long splitMD5(byte[] md5, int begin){
            return ((md5[begin] & 0xFFL) << 24) | ((md5[begin + 1] & 0xFFL) << 16) | ((md5[begin + 2] & 0xFFL) << 8) | (md5[begin + 3] & 0xFF);
        }
    }

    //服务名 -- Selector
    private final ConcurrentMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    public InetSocketAddress select(List<InetSocketAddress> serviceInstances, RpcRequest rpcRequest) {
        if(serviceInstances.isEmpty()) return null;

        //快速判断提供者列表的改变(极小概率失败)
        int identityHashCode = serviceInstances.hashCode();

        ConsistentHashSelector selector = selectors.get(rpcRequest.getServiceName());

        if(selector == null || selector.getIdentityHashCode() != identityHashCode){
            selector = new ConsistentHashSelector(serviceInstances, identityHashCode);
            selectors.put(rpcRequest.getServiceName(), selector);
        }

        return selector.select(rpcRequest);
    }
}