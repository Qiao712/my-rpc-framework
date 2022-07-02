package qiao712.service;

import qiao712.domain.Hello;

public interface TestService {
    Integer add(Integer a, Integer b);
    Integer add(Integer a, Integer b, Integer c);
    Hello hello(Hello hi);
    String hello();
}
