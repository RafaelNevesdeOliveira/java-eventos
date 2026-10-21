package br.com.fiap.b3.aula2.controller.scheduling;

import br.com.fiap.b3.aula2.domain.usecase.PublishPendingEventsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Aciona periodicamente a publicação dos eventos pendentes.
 *
 * <p>Quem chama: agendador Spring no intervalo lab.relay-delay-ms.
 *
 * <p>Quem é chamado: PublishPendingEventsUseCase; não contém SQL ou decisão de deduplicação.
 */
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
