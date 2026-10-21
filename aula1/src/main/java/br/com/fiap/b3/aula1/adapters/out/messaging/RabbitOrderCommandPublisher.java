package br.com.fiap.b3.aula1.adapters.out.messaging;

import br.com.fiap.b3.aula1.adapters.contract.OrderCommand;
import br.com.fiap.b3.aula1.application.port.out.OrderCommandPublisher;
import br.com.fiap.b3.aula1.infrastructure.config.Topology;
import br.com.fiap.b3.ordens.domain.Order;
import java.util.concurrent.TimeUnit;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RabbitOrderCommandPublisher implements OrderCommandPublisher {
  private final RabbitTemplate rabbit;
  private final ObjectMapper json;

  public RabbitOrderCommandPublisher(RabbitTemplate rabbit, ObjectMapper json) {
    this.rabbit = rabbit;
    this.json = json;
  }

  public void publish(Order order) {
    try {
      var confirmation = new CorrelationData(order.id().toString());
      rabbit.convertAndSend(
          "", Topology.QUEUE, json.writeValueAsString(OrderCommand.from(order)), confirmation);
      var ack = confirmation.getFuture().get(10, TimeUnit.SECONDS);
      if (!ack.ack() || confirmation.getReturned() != null)
        throw new IllegalStateException("Comando não confirmado/roteado");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Publicação interrompida", e);
    } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
      throw new IllegalStateException("Falha publicando comando", e);
    }
  }
}
