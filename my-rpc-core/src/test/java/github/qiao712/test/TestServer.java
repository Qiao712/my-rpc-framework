package github.qiao712.test;

import github.qiao712.entity.RpcRequest;
import github.qiao712.entity.RpcResponse;
import github.qiao712.registry.ServiceRegistry;
import github.qiao712.registry.SimpleServiceRegistry;
import github.qiao712.provider.DefaultRequestHandler;
import github.qiao712.provider.RequestHandler;
import org.junit.Test;

public class TestServer {
    public static class TestService{
        public String test(){
            return "test";
        }

        public String testThrow(){
            throw new RuntimeException("test error");
        }
    }

    @Test
    public void testRequestHandler() throws NoSuchMethodException {
        ServiceRegistry serviceRegistry = new SimpleServiceRegistry();
        serviceRegistry.register("testService", new TestService());
        RequestHandler requestHandler = new DefaultRequestHandler(serviceRegistry);

        RpcRequest request = new RpcRequest("testService", "test", null);
        RpcResponse response = requestHandler.handleRequest(request);
        System.out.println(response.getData());

        request = new RpcRequest("testService", "testThrow", null);
        response = requestHandler.handleRequest(request);
        System.out.println(response.getCode());
    }
}
