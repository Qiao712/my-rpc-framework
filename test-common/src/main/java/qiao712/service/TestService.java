package qiao712.service;

import qiao712.domain.Hello;

import java.util.List;

public interface TestService {
    Integer add(Integer a, Integer b);
    Integer add(Integer a, Integer b, Integer c);
    int add2(int a, int b);
    int sum(List<Integer> nums);
    Hello hello(Hello hi);
    String hello();
    void delay(Integer time);
    void testThrow();

    //测试调用次数
    void count();
}
