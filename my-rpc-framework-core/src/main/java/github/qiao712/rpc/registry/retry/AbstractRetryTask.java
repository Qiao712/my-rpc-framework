package github.qiao712.rpc.registry.retry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRetryTask implements RetryTask{
    private volatile boolean cancel = false;

    public void cancel(){
        cancel = true;
    }

    public boolean isCanceled(){
        return cancel;
    }
}