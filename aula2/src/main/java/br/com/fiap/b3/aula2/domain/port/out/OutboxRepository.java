package br.com.fiap.b3.aula2.domain.port.out;

import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.util.List;
import java.util.UUID;

/**
 * Define inclusão, bloqueio, marcação e reenvio de eventos da Outbox.
 *
 * <p>Quem chama: CreateOrderUseCase, PublishPendingEventsUseCase e RequeueEventUseCase.
 *
 * <p>Quem é chamado: JdbcOutboxRepository; separa a política de entrega do SQL.
 */
public interface OutboxRepository {
  void append(OrderAccepted event);

  List<OrderAccepted> lockPending(int limit);

  void markPublished(UUID eventId);

  boolean requeue(UUID eventId);
}
