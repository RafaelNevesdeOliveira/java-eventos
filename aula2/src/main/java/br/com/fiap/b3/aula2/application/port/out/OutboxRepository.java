package br.com.fiap.b3.aula2.application.port.out;

import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository {
  void append(OrderAccepted event);

  List<OrderAccepted> lockPending(int limit);

  void markPublished(UUID eventId);

  boolean requeue(UUID eventId);
}
