package github.qiao712.rpc.registry.retry;

public interface RetryTask{
    void retry() throws Exception;

    boolean isCanceled();

    void cancel();
}
