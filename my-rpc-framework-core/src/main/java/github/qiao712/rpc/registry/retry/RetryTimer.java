package github.qiao712.rpc.registry.retry;

import java.util.concurrent.*;

public class RetryTimer {
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
    private final int delay;           //Millisecond

    public RetryTimer(int delay){
        this.delay = delay;
    }

    public void addRetryTask(RetryTask retryTask){
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                if(!retryTask.isCanceled()){
                    try{
                        retryTask.retry();
                        retryTask.cancel(); //成功后取消
                    }catch (Throwable e){
                        //失败重试
                        executor.schedule(this, delay, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public void cancel(){
        executor.shutdown();
    }
}
