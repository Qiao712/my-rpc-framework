package github.qiao712.service;

import qiao712.domain.Hello;
import qiao712.service.TestService;

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
}
