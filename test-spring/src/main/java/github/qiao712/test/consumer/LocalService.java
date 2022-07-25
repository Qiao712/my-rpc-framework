package github.qiao712.test.consumer;

import github.qiao712.annotation.RpcServiceReference;
import github.qiao712.test.provider.service.TestService;
import org.springframework.stereotype.Component;

@Component
public class LocalService {
    @RpcServiceReference
    private TestService testService;

//    @RpcServiceReference
//    private LocalService2 testService2;

    public void testRpc(){
        System.out.println(testService.add(123, 123));
        testService.print();
        testService.throwException();
    }
}
