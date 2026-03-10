package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.proto.RpcResponseCode;
import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.transport.RpcClient;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 失败自动重连集群
 * 使用 Netty 的 HashedWheelTimer 时间轮进行异步重试
 */
@Slf4j
public class FailbackCluster extends AbstractCluster {

    private static final int DEFAULT_RETRY_INTERVAL = 3000; // 默认重试间隔 3 秒
    private static final int DEFAULT_MAX_RETRIES = 3; // 默认最大重试次数

    private final HashedWheelTimer timer;
    private int retryInterval = DEFAULT_RETRY_INTERVAL;
    private int maxRetries = DEFAULT_MAX_RETRIES;

    public FailbackCluster(RpcClient rpcClient, ServiceRegistry serviceRegistry, LoadBalance loadBalance) {
        super(rpcClient, serviceRegistry, loadBalance);
        this.timer = new HashedWheelTimer();
        this.timer.start();
    }

    @Override
    protected RpcResponse doInvoke(List<ProviderURL> providers, RpcRequest rpcRequest) {
        if (providers.isEmpty()) {
            throw new RpcException("无可用服务提供者");
        }

        try {
            // 直接选择一个节点进行调用
            ProviderURL selected = loadBalance.select(providers, rpcRequest);
            return doRequest(selected, rpcRequest);
        } catch (RpcException e) {
            // 调用失败，加入时间轮进行异步重试
            log.warn("调用失败，加入重试队列：{}", rpcRequest.getMethodName(), e);

            // 提交到时间轮进行重试
            submitRetryTask(providers, rpcRequest, 0);

            // 立即返回一个带有空值的响应
            RpcResponse response = new RpcResponse();
            response.setCode(RpcResponseCode.SUCCESS);
            response.setData(null);
            return response;
        }
    }

    /**
     * 提交重试任务到时间轮
     */
    private void submitRetryTask(List<ProviderURL> providers, RpcRequest rpcRequest, int retryCount) {
        timer.newTimeout(timeout -> handleRetry(timeout, providers, rpcRequest, retryCount),
                retryInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 处理重试逻辑
     */
    private void handleRetry(Timeout timeout, List<ProviderURL> providers, RpcRequest rpcRequest, int retryCount) {
        if (timeout.isCancelled()) {
            return;
        }

        if (retryCount >= maxRetries) {
            log.error("达到最大重试次数，放弃重试：{}", rpcRequest.getMethodName());
            return;
        }

        try {
            ProviderURL selected = loadBalance.select(providers, rpcRequest);
            RpcResponse response = doRequest(selected, rpcRequest);

            if (response.getCode() == github.qiao712.rpc.proto.RpcResponseCode.SUCCESS) {
                log.info("重试成功：{}", rpcRequest.getMethodName());
            } else {
                log.warn("重试失败，继续下一次重试：{}", rpcRequest.getMethodName());
                submitRetryTask(providers, rpcRequest, retryCount + 1);
            }
        } catch (RpcException e) {
            log.warn("重试异常，继续下一次重试：{}", rpcRequest.getMethodName(), e);
            submitRetryTask(providers, rpcRequest, retryCount + 1);
        }
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        if (retryInterval <= 0) {
            throw new IllegalArgumentException("retryInterval must be positive");
        }
        this.retryInterval = retryInterval;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
        this.maxRetries = maxRetries;
    }
}