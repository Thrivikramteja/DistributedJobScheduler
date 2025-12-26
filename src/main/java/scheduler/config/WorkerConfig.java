package scheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerConfig {
    @Value("${scheduler.worker.id:${HOSTNAME:localhost}-${PID:0}}")
    private String workerId;

    @Bean
    public String workerId() {
        return workerId;
    }
}
