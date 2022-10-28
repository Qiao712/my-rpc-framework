package github.qiao712.rpc.handler;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ServiceProvider;

import java.util.concurrent.*;

/**
 * 使用一个线程串行化的执行所有RPC请求，不会让两个请求同时执行
 */
public class SerializableRequestHandler implements RequestHandler{
    private final SimpleRequestHandler simpleRequestHandler;
    private final ExecutorService executorService;

    public SerializableRequestHandler(ServiceProvider serviceProvider, int queueLength){
        this.simpleRequestHandler = new SimpleRequestHandler(serviceProvider);

        executorService = new ThreadPoolExecutor(1,1,0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueLength), new ThreadPoolExecutor.AbortPolicy());
    }

    public SerializableRequestHandler(ServiceProvider serviceProvider){
        this.simpleRequestHandler = new SimpleRequestHandler(serviceProvider);

        executorService = new ThreadPoolExecutor(1,1,0, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    public void handleRequest(RpcRequest request, ResponseSender responseSender) {
        executorService.execute(()->{
            simpleRequestHandler.handleRequest(request, responseSender);
        });
    }
}
