package github.qiao712;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qiao712.service.TestService;

import java.io.IOException;

public class TestClient {
    private static final Logger log = LoggerFactory.getLogger(TestClient.class);

    public static void main(String[] args) throws IOException {
        RpcClient rpcClient = new RpcClient("127.0.0.1", 9712);
        TestService testService = rpcClient.getProxy(TestService.class);

        System.out.println(testService.add(123, 123));
        System.out.println(testService.add(123,123,123));

        long n = 1000;
        long begin = System.nanoTime();
        for(int i = 0; i < n; i++){
            testService.add(231, 123);
        }
        long end = System.nanoTime();

        log.debug("{}次调用耗时{}ms", n, (end-begin)/1000.0);
//        System.out.println(n + "次调用耗时:" + (end - begin) + "ns");
    }
}
