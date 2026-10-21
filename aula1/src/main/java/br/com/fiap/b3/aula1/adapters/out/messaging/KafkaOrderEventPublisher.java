package br.com.fiap.b3.aula1.adapters.out.messaging;

import br.com.fiap.b3.aula1.adapters.contract.AcceptedEventMessage;
import br.com.fiap.b3.aula1.application.port.out.OrderEventPublisher;
import br.com.fiap.b3.aula1.infrastructure.config.Topology;
import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.util.concurrent.TimeUnit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class KafkaOrderEventPublisher implements OrderEventPublisher {
  private final KafkaTemplate<String, String> kafka;
  private final ObjectMapper json;

  public KafkaOrderEventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper json) {
    this.kafka = kafka;
    this.json = json;
  }

  public void publish(OrderAccepted event) {
    try {
      kafka
          .send(
              Topology.TOPIC,
              event.order().contaId(),
              json.writeValueAsString(AcceptedEventMessage.from(event)))
          .get(15, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Publicação interrompida", e);
    } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
      throw new IllegalStateException("Falha publicando evento", e);
    }
  }
}
