package github.qiao712.test.service;

import github.qiao712.annotation.RpcService;
import qiao712.domain.Hello;
import qiao712.service.TestService;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RpcService(weight = 40)
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
    public int sum(List<Integer> nums) {
        int sum = 0;
        for (Integer num : nums) {
            sum += num;
        }
        return sum;
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

    private final AtomicLong counter = new AtomicLong();
    @Override
    public void count() {
        long l = counter.incrementAndGet();
        System.out.println("---------------调用次数: " + l + "-----------------------------");
    }
}
