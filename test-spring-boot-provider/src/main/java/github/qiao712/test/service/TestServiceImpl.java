package github.qiao712.test.service;

import github.qiao712.annotation.RpcService;
import qiao712.domain.Hello;
import qiao712.service.TestService;

@RpcService
public class TestServiceImpl implements TestService {

    @Override
    public Integer add(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public Integer add(Integer a, Integer b, Integer c) {
        return a + b + c;
    }

    @Override
    public int add2(int a, int b) {
        return a + b;
    }

    @Override
    public Hello hello(Hello hi) {
        System.out.println(hi);
        Hello hi2 = new Hello();
        hi2.setId(123);
        hi2.setHello("Hello from Provider");
        return hi2;
    }

    @Override
    public String hello() {
        System.out.println("hello..");
        return "hellooooo";
    }

    @Override
    public void delay(Integer time){
        System.out.println("等待" + time + "ms");
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void testThrow() {
        throw new ArithmeticException("test exception");
    }
}
