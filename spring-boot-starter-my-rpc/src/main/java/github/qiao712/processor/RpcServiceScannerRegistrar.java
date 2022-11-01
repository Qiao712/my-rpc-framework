package github.qiao712.processor;

import github.qiao712.annotation.EnableRpcServer;
import github.qiao712.annotation.RpcService;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * 注册指定包及其子包下的服务实现类 (@RpcService)
 */
public class RpcServiceScannerRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //获取导入该类的类的注解信息
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRpcServer.class.getName()));

        //要扫描的包
        String[] basePackages = new String[0];
        if(annotationAttributes != null){
            basePackages = annotationAttributes.getStringArray("value");

            //若未配置要扫描的包，默认扫描被注解的类所在的包
            if(basePackages.length == 0){
                String className = importingClassMetadata.getClassName();
                int p = className.lastIndexOf('.');
                if(p == -1){
                    basePackages = new String[]{""};
                }else{
                    basePackages = new String[]{className.substring(0, p)};
                }
            }
        }

        //扫描并注册
        if(basePackages.length != 0){
            //扫描@RpcService注解的类
            ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner(registry, false);
            classPathBeanDefinitionScanner.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
            int scan = classPathBeanDefinitionScanner.scan(basePackages);
        }
    }
}
