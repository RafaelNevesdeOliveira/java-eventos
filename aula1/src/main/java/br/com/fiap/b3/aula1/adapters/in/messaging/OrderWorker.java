package br.com.fiap.b3.aula1.adapters.in.messaging;

import br.com.fiap.b3.aula1.adapters.contract.OrderCommand;
import br.com.fiap.b3.aula1.application.usecase.AcceptOrderUseCase;
import br.com.fiap.b3.aula1.infrastructure.config.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrderWorker {
  private static final Logger log = LoggerFactory.getLogger(OrderWorker.class);
  private final AcceptOrderUseCase accept;
  private final ObjectMapper json;

  public OrderWorker(AcceptOrderUseCase accept, ObjectMapper json) {
    this.accept = accept;
    this.json = json;
  }

  @RabbitListener(queues = Topology.QUEUE)
  public void execute(String payload) {
    var command = json.readValue(payload, OrderCommand.class);
    accept.execute(command.toDomain());
    log.info(
        "WORKER={} comando={} evento publicado",
        Thread.currentThread().getName(),
        command.ordemId());
  }
}
