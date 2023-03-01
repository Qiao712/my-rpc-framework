package github.qiao712.test;

import github.qiao712.rpc.registry.retry.AbstractRetryTask;
import github.qiao712.rpc.registry.retry.RetryTask;
import github.qiao712.rpc.registry.retry.RetryTimer;
import org.junit.Test;

public class TestRetry {
    @Test
    public void testRetry() throws InterruptedException {
        System.out.println("test");
        RetryTimer retryTimer = new RetryTimer(1000);

        RetryTask t1 = new AbstractRetryTask(){
            @Override
            public void retry() throws Exception {
                System.out.println("t1");
                throw new Exception();
            }
        };
        RetryTask t2 = new AbstractRetryTask(){
            @Override
            public void retry() throws Exception {
                System.out.println("t2");
                throw new Exception();
            }
        };
        RetryTask t3 = new AbstractRetryTask(){
            @Override
            public void retry() throws Exception {
                System.out.println("t3");
            }
        };
        retryTimer.addRetryTask(t1);
        retryTimer.addRetryTask(t2);
        retryTimer.addRetryTask(t3);

        Thread.sleep(10000);

        retryTimer.cancel();
    }
}
