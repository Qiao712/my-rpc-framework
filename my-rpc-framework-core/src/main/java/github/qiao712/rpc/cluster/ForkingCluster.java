package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.transport.RpcClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 同时调用多个服务提供者，只要其中一个返回，就立即返回结果
 * 若全部调用失败，只返回最后一次的异常
 */
public class ForkingCluster extends AbstractCluster{
    //并发调用的服务数量
    private final static int DEFAULT_FORKS = Runtime.getRuntime().availableProcessors();
    private int forks = DEFAULT_FORKS;

    private final ExecutorService executor = new ThreadPoolExecutor(
            2 * DEFAULT_FORKS,
            10 * DEFAULT_FORKS,
            10, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public ForkingCluster(RpcClient rpcClient, ServiceRegistry serviceRegistry, LoadBalance loadBalance) {
        super(rpcClient, serviceRegistry, loadBalance);
    }

    @Override
    protected RpcResponse doInvoke(List<ProviderURL> providers, RpcRequest rpcRequest) {
        if(providers.isEmpty()){
            throw new RpcException("请求失败: 无可用服务提供者.");
        }

        //选出足够的节点
        List<ProviderURL> selected;
        if(providers.size() < forks){
            selected = providers;
        }else{
            selected = new ArrayList<>(forks);
            List<ProviderURL> unselected = new ArrayList<>(providers);

            while(selected.size() < forks){
                ProviderURL select = loadBalance.select(unselected, rpcRequest);
                unselected.remove(select);
                selected.add(select);
            }
        }

        //并发的调用
        AtomicInteger failCount = new AtomicInteger(0);
        CompletableFuture<Object> future = new CompletableFuture<>();
        for (ProviderURL provider : selected) {
            executor.execute(()->{
                try{
                    if(future.isDone()) return;    //已经调用成功
                    RpcResponse rpcResponse = doRequest(provider, rpcRequest);
                    future.complete(rpcResponse);
                }catch (RpcException e){
                    //当最后一次调用失败时，返回异常
                    int count = failCount.incrementAndGet();
                    if(count >= selected.size()){
                        future.completeExceptionally(e);
                    }
                }
            });
        }

        //获取结果或最后一次的异常
        try {
            Object result = future.get();
            return (RpcResponse) result;
        } catch (InterruptedException e) {
            throw new RpcException("调用被中断");
        } catch (ExecutionException e) {
            throw new RpcException("调用失败. 最后一次失败原因" + e.getMessage(), e);
        }
    }

    public int getForks() {
        return forks;
    }

    public void setForks(int forks) {
        if(forks < 1){
            throw new IllegalArgumentException("forks < 1");
        }
        this.forks = forks;
    }
}
