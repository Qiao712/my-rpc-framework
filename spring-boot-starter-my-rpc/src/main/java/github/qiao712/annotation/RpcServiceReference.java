package github.qiao712.annotation;

import github.qiao712.rpc.cluster.Cluster;
import github.qiao712.rpc.cluster.FailoverCluster;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.loadbalance.RandomLoadBalance;

import java.lang.annotation.*;

/**
 * 引用服务
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcServiceReference {
    Class<? extends LoadBalance> loadbalance() default RandomLoadBalance.class;

    Class<? extends Cluster> cluster() default FailoverCluster.class;

    //容错策略为Failover时的重试次数
    int retries() default 10;

    //容错策略为Forking同时请求的提供者的数量
    int forks() default 10;
}
