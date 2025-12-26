package scheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class PoolConfig {

    @Value("${scheduler.executor.pool-size:4}")
    int poolSize;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService jobExecutor() {
        return Executors.newFixedThreadPool(poolSize);
    }
}
