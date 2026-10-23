package br.com.fiap.b3.aula1.controller.messaging;

import br.com.fiap.b3.aula1.domain.port.out.OrderCommandReceiver;
import br.com.fiap.b3.aula1.domain.usecase.AcceptOrderUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Coordena a entrada por polling no perfil OCI usando apenas portas e casos de uso do domínio.
 *
 * <p>Quem chama: agendador Spring. Quem é chamado: receptor, AcceptOrderUseCase e confirmação.
 * Falha na publicação mantém a mensagem sem ack; nunca confirma antes do evento no Streaming.
 */
@Component
@Profile("oci")
public class OciOrderWorker {
  private static final Logger log = LoggerFactory.getLogger(OciOrderWorker.class);
  private final OrderCommandReceiver commands;
  private final AcceptOrderUseCase accept;

  public OciOrderWorker(OrderCommandReceiver commands, AcceptOrderUseCase accept) {
    this.commands = commands;
    this.accept = accept;
  }

  @Scheduled(fixedDelayString = "${lab.oci.queue-poll-delay-ms:1000}")
  public void execute() {
    try {
      for (var delivery : commands.receive()) {
        accept.execute(delivery.order());
        commands.acknowledge(delivery.receipt());
        log.info("OCI_QUEUE_CONFIRMADO comando={} evento publicado", delivery.order().id());
      }
    } catch (RuntimeException error) {
      // Não registra payloads, comprovantes ou credenciais; a mensagem continua disponível para retry.
      log.warn("OCI Queue: processamento sem confirmação; verificar sessão e conectividade. Tipo={}",
          error.getClass().getSimpleName());
    }
  }
}
