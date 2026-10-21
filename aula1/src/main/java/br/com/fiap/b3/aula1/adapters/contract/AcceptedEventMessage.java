package br.com.fiap.b3.aula1.adapters.contract;

import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.util.UUID;

public record AcceptedEventMessage(UUID id, String type, String time, OrderCommand data) {
  public static AcceptedEventMessage from(OrderAccepted event) {
    return new AcceptedEventMessage(
        event.id(),
        "br.com.b3.ordem.aceita.v1",
        event.occurredAt().toString(),
        OrderCommand.from(event.order()));
  }
}
