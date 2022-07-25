package github.qiao712.annotation;

import github.qiao712.processor.RpcServiceReferenceProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启桩对象的自动注入(自动注入 @RpcServiceReference 注解的字段)
 * 注册RpcServiceReferenceProcessor 用于 处理@RpcServiceReference并注入
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import(RpcServiceReferenceProcessor.class)
public @interface EnableRpcService {
}
