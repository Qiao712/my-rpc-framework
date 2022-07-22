package github.qiao712.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcServiceReference {
    String loadbalance() default "random";

    String cluster() default "failover";

    int retries() default 10;
}
