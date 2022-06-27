package github.qiao712.service;

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
}
