package br.com.fiap.b3.aula2.controller.messaging;

import br.com.fiap.b3.aula2.domain.usecase.ApplyOrderAcceptedUseCase;
import br.com.fiap.b3.aula2.presenter.contract.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Recebe CloudEvents Kafka e coordena sua aplicação na projeção.
 *
 * <p>Quem chama: listener Kafka do grupo aula2-posicao.
 *
 * <p>Quem é chamado: CloudEvent.toDomain e ApplyOrderAcceptedUseCase; retorna após a transação SQL.
 */
@Component
public class PositionConsumer {
  private static final Logger log = LoggerFactory.getLogger(PositionConsumer.class);
  private final ApplyOrderAcceptedUseCase apply;
  private final ObjectMapper json;

  public PositionConsumer(ApplyOrderAcceptedUseCase apply, ObjectMapper json) {
    this.apply = apply;
    this.json = json;
  }

  @KafkaListener(topics = "aula2.ordens.aceitas.v1", groupId = "aula2-posicao")
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
