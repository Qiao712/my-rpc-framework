package github.qiao712.test.consumer;

import github.qiao712.annotation.RpcServiceReference;
import org.springframework.stereotype.Component;
import qiao712.service.TestService;

@Component
public class LocalService {
    @RpcServiceReference
    private TestService testService;

    @RpcServiceReference
    private LocalService2 testService2;

    public void testRpc(){
//        System.out.println(testService);
//        System.out.println(testService.add(712, 712));
//        testService.hello();

        System.out.println(testService2);
    }
}
