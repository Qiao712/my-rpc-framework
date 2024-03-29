package github.qiao712.annotation;

import github.qiao712.autoconfig.MyRPCClientAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 注册客户端相关组件
 * 开启桩对象的自动注入(自动注入 @RpcServiceReference 注解的字段)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import({MyRPCClientAutoConfiguration.class})
public @interface EnableRpcClient {
}
