package github.qiao712.processor;

import github.qiao712.annotation.RpcService;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * 扫描并注册指定包及其子包下被@RpcService注解的类
 */
public class RpcServiceScanner extends ClassPathBeanDefinitionScanner {
    public RpcServiceScanner(BeanDefinitionRegistry registry) {
        //不使用ClassPathBeanDefinitionScanner类默认的Filter
        super(registry, false);

        //扫描@RpcService注解的类
        this.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
    }
}
