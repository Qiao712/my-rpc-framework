package github.qiao712.factory;

import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.utils.NameUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * LoadBalance的单例工厂
 */
public class LoadBalanceFactory {
    private static final Map<String, LoadBalance> loadBalanceMap = new HashMap<>();

    /**
     * 获取指定名称的LoadBalance对象
     * 获取失败返回null
     */
    public static LoadBalance getLoadBalance(String loadBalanceName){
        LoadBalance loadBalance = loadBalanceMap.get(loadBalanceName);

        if(loadBalance == null){
            loadBalance = newLoadBalanceInstance(loadBalanceName);
            loadBalanceMap.put(loadBalanceName, loadBalance);
        }

        return loadBalance;
    }

    private static LoadBalance newLoadBalanceInstance(String loadBalanceName){
        loadBalanceName = NameUtil.firstLetterToUpperCase(loadBalanceName);
        String className = "github.qiao712.rpc.loadbalance." + loadBalanceName + "LoadBalance";

        System.out.println("create " + className);

        try {
            Class<?> loadBalanceClass = LoadBalanceFactory.class.getClassLoader().loadClass(className);
            Constructor<?> constructor = loadBalanceClass.getConstructor();
            Object loadBalance = constructor.newInstance();

            if(loadBalance instanceof LoadBalance){
                return (LoadBalance) loadBalance;
            }
        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }

        return null;
    }
}
