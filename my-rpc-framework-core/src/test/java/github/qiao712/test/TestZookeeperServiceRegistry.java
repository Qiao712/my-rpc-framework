package github.qiao712.test;

import github.qiao712.rpc.registry.AbstractServiceRegistry;
import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceRegistry;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class TestZookeeperServiceRegistry {
    private InetSocketAddress zkAddress = new InetSocketAddress("114.116.245.83", 2181);

    //测试服务发现
    @Test
    public void testDiscovery() throws InterruptedException, IOException {
        ServiceRegistry registry = new ZookeeperServiceRegistry(zkAddress);

        System.out.println("开始订阅");
        registry.subscribe("test.Service1");
        registry.subscribe("test.Service2");
        System.out.println("订阅完成");

        //等待
        List<ProviderURL> providers = registry.getProviders("test.Service1");
        while(providers.isEmpty()){
            Thread.sleep(1000);
            providers = registry.getProviders("test.Service1");
        }

        while(true){
            System.out.println("test.Service1的实例列表:");
            providers = registry.getProviders("test.Service1");
            for (ProviderURL provider : providers) {
                System.out.println(provider);
            }

            System.out.println("test.Service2的实例列表:");
            providers = registry.getProviders("test.Service2");
            for (ProviderURL provider : providers) {
                System.out.println(provider);
            }

            int read = System.in.read();
        }
    }

    //测试服务注册
    @Test
    public void testRegistry() throws InterruptedException {
        ZookeeperServiceRegistry registry = new ZookeeperServiceRegistry(new InetSocketAddress("114.116.245.83", 2181));

        ProviderURL providerURL1 = new ProviderURL();
        providerURL1.setService("test.Service1");
        providerURL1.setAddress(new InetSocketAddress("localhost", 8888));
        providerURL1.setWeight(123);
        registry.register(providerURL1);

        ProviderURL providerURL2 = new ProviderURL();
        providerURL2.setService("test.Service2");
        providerURL2.setAddress(new InetSocketAddress("localhost", 9999));
        providerURL2.setWeight(100);
        registry.register(providerURL2);

        Thread.sleep(100000000);

        registry.close();
    }

    static class AServiceRegistry extends AbstractServiceRegistry {
        int i = 0, j = 0;
        @Override
        protected void doRegister(ProviderURL providerURL) throws Exception {
            if(i++ < 10){
                System.out.println(providerURL + " 注册失败");
                throw new Exception("测试 注册失败");
            }
            System.out.println("注册成功");
        }

        @Override
        protected void doUnregister(ProviderURL providerURL) throws Exception {
            if(j++ < 10){
                System.out.println(providerURL + "取消失败");
                throw new Exception("测试 取消失败");
            }
            System.out.println("取消成功");
        }

        @Override
        protected void doSubscribe(String serviceName) throws Exception {

        }

        @Override
        protected void doUnsubscribe(String serviceName) throws Exception {

        }

        @Override
        protected List<ProviderURL> doGetProviders(String serviceName) {
            return null;
        }

        @Override
        public void close() {
        }
    }

    @Test
    public void testRetry() throws InterruptedException {
        AServiceRegistry aRegistry = new AServiceRegistry();
        aRegistry.register(new ProviderURL(new InetSocketAddress(123), "123", 123));
        aRegistry.register(new ProviderURL(new InetSocketAddress(12), "124", 124));
        Thread.sleep(300);
        aRegistry.unregister(new ProviderURL(new InetSocketAddress(123), "123", 123));
        aRegistry.unregister(new ProviderURL(new InetSocketAddress(12), "124", 124));
        Thread.sleep(3000);
        aRegistry.register(new ProviderURL(new InetSocketAddress(123), "123", 123));
        Thread.sleep(3000);
    }
}
