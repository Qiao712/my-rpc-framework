package github.qiao712.test;

import github.qiao712.annotation.RpcServiceReference;
import github.qiao712.rpc.loadbalance.ConsistentHashLoadBalance;
import github.qiao712.rpc.loadbalance.RandomLoadBalance;
import github.qiao712.rpc.loadbalance.RoundRobinLoadBalance;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import qiao712.domain.Hello;
import qiao712.service.TestService;

import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

@Component
public class TestConsumer{
    @RpcServiceReference(loadbalance = RoundRobinLoadBalance.class)
    private TestService testService;

    public void testLoadBalance(){
        for(int i = 0; i < 10000; i++){
            testService.count();
        }
    }

    public void testRPC(){
        System.out.println("---测试调用---");
        System.out.println(testService.hello());

        Hello hello = new Hello();
        hello.setHello("hello rpc");
        testService.hello(hello);

        System.out.println(testService.add(123, 123));

        System.out.println(testService.sum(Arrays.asList(1, 2, 3, 4, 5)));

        testService.testThrow();
    }
}
