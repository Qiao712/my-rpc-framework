package github.qiao712.annotation;

import java.lang.annotation.*;

/**
 * 服务实现类注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {
}
