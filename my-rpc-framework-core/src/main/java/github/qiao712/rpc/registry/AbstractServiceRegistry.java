package github.qiao712.rpc.registry;

import github.qiao712.rpc.exception.RpcFrameworkException;
import github.qiao712.rpc.registry.retry.AbstractRetryTask;
import github.qiao712.rpc.registry.retry.RetryTask;
import github.qiao712.rpc.registry.retry.RetryTimer;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 实现注册、取消注册、订阅、取消订阅的失败重试与恢复
 */
@Slf4j
public abstract class AbstractServiceRegistry implements ServiceRegistry {
    //所有注册的服务提供者信息，注册中心最终应与该列表一致
    private final Set<ProviderURL> registered = Collections.synchronizedSet(new HashSet<>());

    //所有订阅的服务
    private final Set<String> subscribed = Collections.synchronizedSet(new HashSet<>());

    //各种失败的重试任务列表
    private final ConcurrentMap<ProviderURL, RetryTask> failedRegistered = new ConcurrentHashMap<>();
    private final ConcurrentMap<ProviderURL, RetryTask> failedUnregistered = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RetryTask> failedSubscribed = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RetryTask> failedUnsubscribed = new ConcurrentHashMap<>();
    private final RetryTimer retryTimer = new RetryTimer(1000);

    @Override
    public synchronized void register(ProviderURL providerURL) {
        //先维护本地的列表
        registered.add(providerURL);
        //清除旧的重试任务
        removeFailedRegistered(providerURL);
        removeFailedUnregistered(providerURL);
        try{
            doRegister(providerURL);
        }catch (Exception e){
            log.info("注册失败:" + providerURL, e);
            //注册失败，启动定时任务进行重试
            addFailedRegistered(providerURL);
        }
    }

    @Override
    public synchronized void unregister(ProviderURL providerURL) {
        registered.remove(providerURL);
        removeFailedRegistered(providerURL);
        removeFailedUnregistered(providerURL);
        try{
            doUnregister(providerURL);
        }catch (Exception e){
            log.info("取消注册失败:" + providerURL, e);
            addFailedUnregistered(providerURL);
        }
    }

    @Override
    public synchronized void subscribe(String serviceName) {
        subscribed.add(serviceName);
        removeFailedSubscribed(serviceName);
        removeFailedUnsubscribed(serviceName);
        try{
            doSubscribe(serviceName);
        }catch (Exception e){
            log.warn("订阅失败:" + serviceName, e);
            addFailedSubscribed(serviceName);
        }
    }

    @Override
    public synchronized void unsubscribe(String serviceName) {
        subscribed.remove(serviceName);
        removeFailedSubscribed(serviceName);
        removeFailedUnsubscribed(serviceName);
        try{
            doUnsubscribe(serviceName);
        }catch (Exception e){
            log.warn("取消订阅失败:" + serviceName, e);
            addFailedUnsubscribed(serviceName);
        }
    }

    @Override
    public List<ProviderURL> getProviders(String serviceName) {
        if(!subscribed.contains(serviceName)){
            throw new RpcFrameworkException("未订阅该服务");
        }
        return doGetProviders(serviceName);
    }

    /**
     * 恢复注册和订阅
     */
    protected synchronized void recover(){
        //恢复注册
        for (ProviderURL providerURL : registered) {
            removeFailedRegistered(providerURL);
            removeFailedUnregistered(providerURL);
            addFailedRegistered(providerURL);
        }
        //恢复订阅
        for (String service : subscribed) {
            removeFailedSubscribed(service);
            removeFailedUnsubscribed(service);
            addFailedSubscribed(service);
        }
    }

    //----- 模板方法 --------------------------------------------------------

    protected abstract void doRegister(ProviderURL providerURL) throws Exception;

    protected abstract void doUnregister(ProviderURL providerURL) throws Exception;

    protected abstract void doSubscribe(String serviceName) throws Exception;

    protected abstract void doUnsubscribe(String serviceName) throws Exception;

    protected abstract List<ProviderURL> doGetProviders(String serviceName);

    //----- 重试任务 --------------------------------------------------------

    //添加注册的重试任务
    private void addFailedRegistered(ProviderURL providerURL){
        RetryTask oldTask = failedRegistered.get(providerURL);
        if(oldTask != null) return;

        RetryTask newTask = new AbstractRetryTask() {
            @Override
            public void retry() throws Exception {
                doRegister(providerURL);
                removeFailedRegistered(providerURL);
            }
        };

        oldTask = failedRegistered.putIfAbsent(providerURL, newTask);
        if(oldTask == null) retryTimer.addRetryTask(newTask);
    }

    //添加取消注册的重试任务
    private void addFailedUnregistered(ProviderURL providerURL){
        RetryTask oldTask = failedUnregistered.get(providerURL);
        if(oldTask != null) return;

        RetryTask newTask = new AbstractRetryTask() {
            @Override
            public void retry() throws Exception {
                doUnregister(providerURL);
                removeFailedUnregistered(providerURL);
            }
        };

        oldTask = failedUnregistered.putIfAbsent(providerURL, newTask);
        if(oldTask == null) retryTimer.addRetryTask(newTask);
    }

    private void addFailedSubscribed(String serviceName) {
        RetryTask oldTask = failedSubscribed.get(serviceName);
        if(oldTask != null) return;

        RetryTask newTask = new AbstractRetryTask() {
            @Override
            public void retry() throws Exception {
                doSubscribe(serviceName);
                removeFailedSubscribed(serviceName);
            }
        };

        oldTask = failedSubscribed.putIfAbsent(serviceName, newTask);
        if(oldTask == null) retryTimer.addRetryTask(newTask);
    }

    private void addFailedUnsubscribed(String serviceName) {
        RetryTask oldTask = failedUnsubscribed.get(serviceName);
        if(oldTask != null) return;

        RetryTask newTask = new AbstractRetryTask() {
            @Override
            public void retry() throws Exception {
                doUnsubscribe(serviceName);
                removeFailedUnsubscribed(serviceName);
            }
        };

        oldTask = failedUnsubscribed.putIfAbsent(serviceName, newTask);
        if(oldTask == null) retryTimer.addRetryTask(newTask);
    }

    private void removeFailedRegistered(ProviderURL providerURL){
        RetryTask task = failedRegistered.remove(providerURL);
        if(task != null) task.cancel();
    }

    private void removeFailedUnregistered(ProviderURL providerURL){
        RetryTask task = failedUnregistered.remove(providerURL);
        if(task != null) task.cancel();
    }

    private void removeFailedSubscribed(String serviceName) {
        RetryTask task = failedSubscribed.remove(serviceName);
        if(task != null) task.cancel();
    }

    private void removeFailedUnsubscribed(String serviceName) {
        RetryTask task = failedUnsubscribed.remove(serviceName);
        if(task != null) task.cancel();
    }
}
