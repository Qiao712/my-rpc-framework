package github.qiao712.test;

import github.qiao712.rpc.handler.RequestHandler;
import github.qiao712.rpc.handler.SimpleRequestHandler;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.registry.ServiceRegistry;
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
    public void testRequestHandler(){
//        ServiceRegistry serviceRegistry = new SimpleServiceRegistry();
//        serviceRegistry.register("testService", new TestService());
//        RequestHandler requestHandler = new SimpleRequestHandler(serviceRegistry);
//
//        RpcRequest request = new RpcRequest("testService", "test", null);
//        RpcResponse response = requestHandler.handleRequest(request);
//        System.out.println(response.getData());
//
//        request = new RpcRequest("testService", "testThrow", null);
//        response = requestHandler.handleRequest(request);
//        System.out.println(response.getCode());
    }
}
