package qiao712.service;

import qiao712.domain.Hello;

public interface TestService {
    Integer add(Integer a, Integer b);
    Integer add(Integer a, Integer b, Integer c);
    int add2(int a, int b);
    Hello hello(Hello hi);
    String hello();
    void delay(Integer time);
    void testThrow();
}
