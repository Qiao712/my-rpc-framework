package github.qiao712.factory;

import github.qiao712.rpc.cluster.Cluster;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;
import github.qiao712.utils.NameUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ClusterFactory {
    /**
     * 根据clusterName选择具体的Cluster实现（构造参数必须与AbstractCluster一致）
     */
    public static Cluster createCluster(String clusterName, RpcClient rpcClient, ServiceDiscovery serviceDiscovery, LoadBalance loadBalance){
        if(clusterName.isEmpty()) return null;

        Cluster cluster = null;
        try {
            clusterName = NameUtil.firstLetterToUpperCase(clusterName);
            String className = "github.qiao712.rpc.cluster." + clusterName + "Cluster";

            Class<?> clusterClass = ClusterFactory.class.getClassLoader().loadClass(className);
            Constructor<?> clusterConstructor = clusterClass.getConstructor(RpcClient.class, ServiceDiscovery.class, LoadBalance.class);
            cluster = (Cluster) clusterConstructor.newInstance(rpcClient, serviceDiscovery, loadBalance);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            return null;
        }

        return cluster;
    }
}
