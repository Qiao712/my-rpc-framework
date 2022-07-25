package github.qiao712.test.provider.service;

import github.qiao712.annotation.RpcService;

@RpcService
public class TestServiceImpl implements TestService {
    @Override
    public Integer add(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public void throwException() {
        throw new ArithmeticException();
    }

    @Override
    public void print() {
        System.out.println("Rpc!");
    }
}
