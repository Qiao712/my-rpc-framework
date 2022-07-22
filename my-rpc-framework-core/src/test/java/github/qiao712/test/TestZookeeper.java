package github.qiao712.test;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class TestZookeeper {
    public CuratorFramework connect(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
        CuratorFramework curator = CuratorFrameworkFactory.builder()
                .connectString("8.141.151.176:2181")
                .retryPolicy(retryPolicy)
                .namespace("my-rpc")
                .build();

        curator.start();

        return curator;
    }

    @Test
    public void testZookeeper() throws Exception {
        CuratorFramework curator = connect();
        System.out.println("connected--------------------------");
        //创建新节点
//        curator.create().creatingParentsIfNeeded().forPath("/1/2/3");

//        curator.setData().forPath("/test-service", "hello".getBytes(StandardCharsets.UTF_8));
//        byte[] bytes = curator.getData().forPath("/test-service");
//        System.out.println(new String(bytes, StandardCharsets.UTF_8));

        //获取子节点
        List<String> paths = curator.getChildren().forPath("/test.Service1/providers");
        for (String path : paths) {
            System.out.println(path);
        }
    }

    @Test
    public void testWatcher() throws Exception {
        CuratorFramework curator = connect();

        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("Event:" + watchedEvent.getType() + " " + watchedEvent.getPath());
            }
        };

        curator.watchers().add().withMode(AddWatchMode.PERSISTENT_RECURSIVE).usingWatcher(watcher).forPath("/test.Service1");

        Thread.sleep(100000000);
    }

    //测试cache监听
    public static void main(String[] args) throws Exception {
        CuratorFramework curator = new TestZookeeper().connect();
        CuratorCache cache = CuratorCache.build(curator, "/test-service");
        CuratorCacheListener listener = CuratorCacheListener.builder().forAll(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldData, ChildData data) {
                System.out.println(type);
            }
        }).build();

//        cache.listenable().addListener(listener);
        cache.start();

        curator.getData().forPath("/test-service");
//        List<String> strings = curator.getChildren().forPath("/test-service");
//        System.out.println("children:");
//        for (String child : strings) {
//            System.out.println(child);
//        }
//        System.out.println("---------------------");

        List<ChildData> collect = cache.stream().collect(Collectors.toList());
        for (ChildData childData : collect) {
            System.out.println(childData.getPath());
        }

        cache.close();
    }
}
