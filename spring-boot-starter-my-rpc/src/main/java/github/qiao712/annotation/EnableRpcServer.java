package github.qiao712.annotation;

import github.qiao712.autoconfig.MyRPCServerAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启动服务端(服务提供者)
 * 扫描并注册为服务提供者
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import({MyRPCServerAutoConfiguration.class})
public @interface EnableRpcServer {
}
