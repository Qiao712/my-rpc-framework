package github.qiao712.test;

import github.qiao712.annotation.EnableRpcServer;
import github.qiao712.rpc.loadbalance.LoadBalance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.List;

@SpringBootApplication
@EnableRpcServer
public class Application {
    public static void main(String[] args) {
        List<String> strings = SpringFactoriesLoader.loadFactoryNames(LoadBalance.class, null);
        System.out.println("SPI----:");
        for (String string : strings) {
            System.out.println(string);
        }

        SpringApplication.run(Application.class, args);
    }
}
