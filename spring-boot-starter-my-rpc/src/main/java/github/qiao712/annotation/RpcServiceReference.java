package github.qiao712.annotation;

import java.lang.annotation.*;

/**
 * 引用服务
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcServiceReference {
    String loadbalance() default "random";

    String cluster() default "failover";

    //容错策略为Failover时的重试次数
    int retries() default 10;

    //容错策略为Forking同时请求的提供者的数量
    int forks() default 10;
}
