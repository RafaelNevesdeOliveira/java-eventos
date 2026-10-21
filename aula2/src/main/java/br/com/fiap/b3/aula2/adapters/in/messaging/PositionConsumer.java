package br.com.fiap.b3.aula2.adapters.in.messaging;

import br.com.fiap.b3.aula2.adapters.contract.CloudEvent;
import br.com.fiap.b3.aula2.application.usecase.ApplyOrderAcceptedUseCase;
import br.com.fiap.b3.aula2.infrastructure.config.MessagingNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class PositionConsumer {
  private static final Logger log = LoggerFactory.getLogger(PositionConsumer.class);
  private final ApplyOrderAcceptedUseCase apply;
  private final ObjectMapper json;

  public PositionConsumer(ApplyOrderAcceptedUseCase apply, ObjectMapper json) {
    this.apply = apply;
    this.json = json;
  }

  @KafkaListener(topics = MessagingNames.ORDER_ACCEPTED_TOPIC, groupId = "aula2-posicao")
  public void consume(String payload) {
    var message = json.readValue(payload, CloudEvent.class);
    boolean applied = apply.execute(message.source(), message.toDomain());
    log.info(
        "{} eventId={} quantidade={}",
        applied ? "POSICAO_ATUALIZADA" : "DUPLICATA_IGNORADA",
        message.id(),
        message.data().quantidade());
  }
}
