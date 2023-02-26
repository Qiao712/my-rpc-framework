package github.qiao712.test;

import github.qiao712.annotation.EnableRpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
@EnableRpcClient
public class Application {
    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext run = SpringApplication.run(Application.class, args);

        TestConsumer bean = run.getBean(TestConsumer.class);
        bean.testLoadBalance();

        System.in.read();

        bean.testLoadBalance();
    }
}
