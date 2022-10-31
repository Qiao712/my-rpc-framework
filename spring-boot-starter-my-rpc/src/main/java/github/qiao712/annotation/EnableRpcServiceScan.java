package github.qiao712.annotation;

import github.qiao712.processor.RpcServiceScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 扫描并注册服务实现类为Bean
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import({RpcServiceScannerRegistrar.class})
public @interface EnableRpcServiceScan {
    /**
     * 指定要扫描的包
     */
    String[] value() default {};
}
