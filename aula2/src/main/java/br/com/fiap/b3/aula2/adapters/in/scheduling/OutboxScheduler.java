package br.com.fiap.b3.aula2.adapters.in.scheduling;

import br.com.fiap.b3.aula2.application.usecase.PublishPendingEventsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduler {
  private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);
  private final PublishPendingEventsUseCase publish;

  public OutboxScheduler(PublishPendingEventsUseCase publish) {
    this.publish = publish;
  }

  @Scheduled(fixedDelayString = "${lab.relay-delay-ms:1500}")
  public void tick() {
    int count = publish.execute();
    if (count > 0) log.info("OUTBOX_PUBLICADA quantidade={}", count);
  }
}
