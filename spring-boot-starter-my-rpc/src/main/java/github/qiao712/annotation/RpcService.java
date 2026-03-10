package github.qiao712.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 服务实现类注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface RpcService {
    //权重
    int weight() default 0;
}
