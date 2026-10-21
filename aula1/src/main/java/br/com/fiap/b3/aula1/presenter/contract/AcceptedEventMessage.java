package br.com.fiap.b3.aula1.presenter.contract;

import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.util.UUID;

/**
 * Monta o formato externo do evento da aula 1.
 *
 * <p>Quem chama: KafkaOrderEventPublisher.
 *
 * <p>Quem é chamado: OrderCommand.from para representar os dados da ordem.
 */
public record AcceptedEventMessage(UUID id, String type, String time, OrderCommand data) {
  public static AcceptedEventMessage from(OrderAccepted event) {
    return new AcceptedEventMessage(
        event.id(),
        "br.com.b3.ordem.aceita.v1",
        event.occurredAt().toString(),
        OrderCommand.from(event.order()));
  }
}
