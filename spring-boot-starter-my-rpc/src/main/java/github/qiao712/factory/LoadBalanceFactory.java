package github.qiao712.factory;

import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.utils.NameUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * LoadBalance的单例工厂
 */
@Slf4j
public class LoadBalanceFactory {
    private static final ConcurrentMap<Class<? extends LoadBalance>, LoadBalance> loadBalanceMap = new ConcurrentHashMap<>();

    /**
     * 创建LoadBalance对象
     * 每次返回的同类型LoadBalance的对象总是同一个
     */
    public static LoadBalance getLoadBalance(Class<? extends LoadBalance> loadBalanceClass){
        return loadBalanceMap.computeIfAbsent(loadBalanceClass, k->{
            try {
                Constructor<? extends LoadBalance> constructor = k.getConstructor();
                return constructor.newInstance();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                log.error("LoadBalance对象构造失败", e);
                return null;
            }
        });
    }
}
