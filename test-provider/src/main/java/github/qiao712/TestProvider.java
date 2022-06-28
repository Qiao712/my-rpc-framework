package github.qiao712;

import github.qiao712.registry.ServiceRegistry;
import github.qiao712.registry.SimpleServiceRegistry;
import github.qiao712.provider.RpcServer;
import github.qiao712.service.TestServiceImpl;

public class TestProvider {
    public static void main(String[] args) {
        ServiceRegistry serviceRegistry = new SimpleServiceRegistry();
        serviceRegistry.register("testService", new TestServiceImpl());
        RpcServer rpcServer = new RpcServer(9712, serviceRegistry);

        rpcServer.start();
    }
}
