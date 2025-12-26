package scheduler.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import scheduler.domain.Job;

@Component
public class LogMessageHandler implements TaskHandler {

    private static final Logger log = LoggerFactory.getLogger(LogMessageHandler.class);

    @Override
    public String getType() {
        return "LOG_MESSAGE";
    }

    @Override
    public void execute(Job job) {
        log.info("[LOG_MESSAGE] task={} priority={} payload={}",
                job.getExternalId(), job.getPriority(), job.getPayload());
    }
}
