package scheduler.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import scheduler.domain.Job;

/**
 * Handles tasks of type {@code SEND_EMAIL}.
 *
 * <p>The task payload is expected to be a JSON string with at least a {@code to} address
 * and a {@code subject}. In a production setup this would integrate with an SMTP relay or
 * a transactional email service (e.g. SendGrid, SES). For now it logs the intent so the
 * full dispatch pipeline can be exercised without external dependencies.
 */
@Component
public class SendEmailHandler implements TaskHandler {

    private static final Logger log = LoggerFactory.getLogger(SendEmailHandler.class);

    @Override
    public String getType() {
        return "SEND_EMAIL";
    }

    @Override
    public void execute(Job job) {
        // In production: parse payload JSON, build MIME message, hand off to mail sender.
        log.info("Dispatching email for task {} | payload: {}", job.getExternalId(), job.getPayload());
    }
}
