package github.qiao712.processor;

import github.qiao712.annotation.RpcService;
import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.registry.ServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * 用于注册服务
 */
@Slf4j
public class RpcServiceBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
    private final ServiceProvider serviceProvider;

    public RpcServiceBeanPostProcessor(ServiceProvider serviceProvider){
        this.serviceProvider = serviceProvider;
    }

    /**
     * 处理服务实现类(被@RpcService注解的类)的Bean
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RpcService rpcServiceAnnotation = AnnotationUtils.findAnnotation(bean.getClass(), RpcService.class);
        if(rpcServiceAnnotation != null){
            try{
                //注册服务
                serviceProvider.addService(bean);

                Class<?>[] interfaces = bean.getClass().getInterfaces();
                if(interfaces.length > 0) log.info("注册服务{}", interfaces[0].getCanonicalName());
            }catch (RpcException e){
                log.error("服务"+beanName+"注册失败", e);
            }
        }

        return bean;
    }
}
