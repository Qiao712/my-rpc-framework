package github.qiao712.rpc.transport.netty.client;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.proto.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 正在等待响应请求的池
 * 维护 请求id 与 等待RpcResponse的Future 的映射
 * 接收到请求后完成该Future并将其移除
 */
@Slf4j
public class WaitingRequestPool {
    private final Map<Integer, CompletableFuture<RpcResponse>> waitingRequests = new ConcurrentHashMap<>();

    /**
     * 等待响应
     * @param requestId 等待响应的请求id
     * @return CompletableFuture<RpcResponse>
     */
    public CompletableFuture<RpcResponse> waitResponse(int requestId){
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();

        if(waitingRequests.containsKey(requestId)){
            throw new RpcException("Request Id冲突");
        }

        waitingRequests.put(requestId, responseFuture);
        return responseFuture;
    }

    /**
     * 完成请求
     * @param requestId 请求id
     * @param rpcResponse 响应该请求的响应
     */
    public void completeRequest(int requestId, RpcResponse rpcResponse){
        CompletableFuture<RpcResponse> responseFuture = waitingRequests.remove(requestId);
        if(responseFuture != null){
            responseFuture.complete(rpcResponse);
        }else{
            log.error("接收到无匹配请求的响应(requestId = " + requestId + ")");
        }
    }

    /**
     * 抛弃请求，不再等待
     */
    public void abandonRequest(int requestId){
        CompletableFuture<RpcResponse> responseFuture = waitingRequests.remove(requestId);
        if(responseFuture == null){
            throw new RpcException("请求不存在(requestId = " + requestId + ")");
        }
    }
}
