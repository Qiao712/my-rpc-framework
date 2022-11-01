package github.qiao712.processor;

import github.qiao712.annotation.RpcServiceReference;
import github.qiao712.factory.ClusterFactory;
import github.qiao712.factory.LoadBalanceFactory;
import github.qiao712.rpc.cluster.Cluster;
import github.qiao712.rpc.cluster.FailoverCluster;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proxy.RpcProxyFactory;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.lang.reflect.Field;

public class RpcServiceReferenceBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
    private final RpcProxyFactory rpcProxyFactory;
    private final RpcClient rpcClient;
    private final ServiceDiscovery serviceDiscovery;

    public RpcServiceReferenceBeanPostProcessor(RpcProxyFactory rpcProxyFactory, RpcClient rpcClient, ServiceDiscovery serviceDiscovery) {
        this.rpcProxyFactory = rpcProxyFactory;
        this.rpcClient = rpcClient;
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 处理注解为@RpcServiceReference的域
     * 创建并注入代理对象(桩对象)
     */
    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            RpcServiceReference serviceReference = field.getAnnotation(RpcServiceReference.class);
            if(serviceReference != null){
                //获取LoadBalance策略对象
                LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(serviceReference.loadbalance());
                if(loadBalance == null){

                }

                //创建Cluster
                Cluster cluster = ClusterFactory.createCluster(serviceReference.cluster(), rpcClient, serviceDiscovery, loadBalance);
                if(cluster instanceof FailoverCluster){
                    //设置重试次数
                    ((FailoverCluster) cluster).setRetries(serviceReference.retries());
                }

                //订阅服务
                serviceDiscovery.subscribeService(field.getType().getCanonicalName());

                //创建桩对象
                Object proxy = rpcProxyFactory.createProxy(field.getType(), field.getType().getCanonicalName(), cluster);

                //注入
                try {
                    field.setAccessible(true);
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    //
                }
            }
        }

        return true;
    }
}
