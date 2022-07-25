package github.qiao712.annotation;

import github.qiao712.processor.RpcServiceProcessor;
import github.qiao712.processor.RpcServiceScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 自动扫描服务实现类，实现服务实现类的注册发布
 * 注册RpcServiceScannerRegistrar用于 扫描并注册指定包下的服务实现类(@RpcService)
 * 注册RpcServiceProcessor用于 处理服务实现类(发布服务)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import({RpcServiceScannerRegistrar.class, RpcServiceProcessor.class})
public @interface EnableRpcServiceScan {
    /**
     * 指定要扫描的包
     */
    String[] value() default {};
}
