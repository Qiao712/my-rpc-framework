package github.qiao712.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface EnableRpcServiceScan {
    String[] basePackage();
}
