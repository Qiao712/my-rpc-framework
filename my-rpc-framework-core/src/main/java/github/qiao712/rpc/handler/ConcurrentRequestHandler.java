package github.qiao712.rpc.handler;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ServiceProvider;

import java.util.concurrent.*;

/**
 * 使用线程池
 */
public class ConcurrentRequestHandler extends AbstractRequestHandler{
    private final ExecutorService executorService;

    //代理给SimpleRequestHandler中已经实现的处理函数
    private final SimpleRequestHandler simpleRequestHandler;

    public ConcurrentRequestHandler(ServiceProvider serviceProvider, ExecutorService executorService) {
        super(serviceProvider);
        this.simpleRequestHandler = new SimpleRequestHandler(serviceProvider);
        this.executorService = executorService;
    }

    @Override
    public void handleRequest(RpcRequest request, ResponseSender responseSender) {
        executorService.execute(()->{
            simpleRequestHandler.handleRequest(request, responseSender);
        });
    }
}
