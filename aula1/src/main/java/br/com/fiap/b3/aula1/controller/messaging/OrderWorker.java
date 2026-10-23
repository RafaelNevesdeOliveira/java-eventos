package br.com.fiap.b3.aula1.controller.messaging;

import br.com.fiap.b3.aula1.domain.usecase.AcceptOrderUseCase;
import br.com.fiap.b3.aula1.presenter.contract.OrderCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Recebe o comando RabbitMQ, converte o contrato e aciona a aceitação.
 *
 * <p>Quem chama: listener RabbitMQ do Spring, com dois workers configurados.
 *
 * <p>Quem é chamado: OrderCommand.toDomain e AcceptOrderUseCase; retorno permite o ack.
 */
@Component
@Profile("!oci")
public class OrderWorker {
  private static final Logger log = LoggerFactory.getLogger(OrderWorker.class);
  private final AcceptOrderUseCase accept;
  private final ObjectMapper json;

  public OrderWorker(AcceptOrderUseCase accept, ObjectMapper json) {
    this.accept = accept;
    this.json = json;
  }

  @RabbitListener(queues = "aula1.ordens.comandos")
  public void execute(String payload) {
    var command = json.readValue(payload, OrderCommand.class);
    accept.execute(command.toDomain());
    log.info(
        "WORKER={} comando={} evento publicado",
        Thread.currentThread().getName(),
        command.ordemId());
  }
}
