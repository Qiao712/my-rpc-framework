package github.qiao712.processor;

import github.qiao712.annotation.EnableRpcServiceScan;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 使用RpcServiceScanner注册指定包及其子包下的服务实现类 (@RpcService)
 */
public class RpcServiceScannerRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //获取导入该类的类的注解信息
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRpcServiceScan.class.getName()));

        //要扫描的包
        String[] basePackages = new String[0];
        if(annotationAttributes != null){
            basePackages = annotationAttributes.getStringArray("value");
        }

        //扫描并注册
        if(basePackages.length != 0){
            RpcServiceScanner rpcServiceScanner = new RpcServiceScanner(registry);
            int scan = rpcServiceScanner.scan(basePackages);
        }
    }
}
