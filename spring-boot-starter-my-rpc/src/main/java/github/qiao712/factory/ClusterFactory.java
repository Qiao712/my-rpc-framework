package github.qiao712.factory;

import github.qiao712.rpc.cluster.Cluster;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;
import github.qiao712.utils.NameUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ClusterFactory {
    private static final ConcurrentMap<Class<? extends Cluster>, Cluster> clusterMap = new ConcurrentHashMap<>();

    /**
     * 创建Cluster对象
     * 总返回不同的对象
     */
    public static Cluster createCluster(Class<? extends Cluster> clusterClass, RpcClient rpcClient, ServiceDiscovery serviceDiscovery, LoadBalance loadBalance){
        return clusterMap.computeIfAbsent(clusterClass, k->{
            try {
                Constructor<? extends Cluster> constructor = k.getConstructor(RpcClient.class, ServiceDiscovery.class, LoadBalance.class);
                return constructor.newInstance(rpcClient, serviceDiscovery, loadBalance);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                log.error("Cluster对象构造失败", e);
                return null;
            }
        });
    }
}
