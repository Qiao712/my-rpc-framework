package github.qiao712.annotation;

import java.lang.annotation.*;

/**
 * 开启桩对象的自动注入(自动注入 @RpcServiceReference 注解的字段)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface EnableRpcService {
}
