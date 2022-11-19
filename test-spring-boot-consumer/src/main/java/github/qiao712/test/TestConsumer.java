package github.qiao712.test;

import github.qiao712.annotation.RpcServiceReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import qiao712.service.TestService;

@Component
public class TestConsumer implements InitializingBean {
    @RpcServiceReference
    private TestService testService;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("测试:");
        if(testService == null){
            System.out.println("注入失败");
            return;
        }

        testLoadBalance();
    }

    public void testLoadBalance(){
        for(int i = 0; i < 10000; i++){
            testService.count();
        }
    }
}
