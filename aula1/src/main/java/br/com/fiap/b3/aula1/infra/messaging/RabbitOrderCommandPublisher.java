package br.com.fiap.b3.aula1.infra.messaging;

import br.com.fiap.b3.aula1.domain.port.out.OrderCommandPublisher;
import br.com.fiap.b3.aula1.infra.config.Topology;
import br.com.fiap.b3.aula1.presenter.contract.OrderCommand;
import br.com.fiap.b3.ordens.domain.Order;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Implementa a publicação do comando com confirmação e verificação de roteamento.
 *
 * <p>Quem chama: SubmitOrderUseCase através de OrderCommandPublisher.
 *
 * <p>Quem é chamado: OrderCommand, ObjectMapper e RabbitTemplate; só retorna após confirmação.
 */
@Component
@Profile("!oci")
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
