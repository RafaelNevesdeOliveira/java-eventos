package br.com.fiap.b3.aula2.domain.usecase;

import br.com.fiap.b3.aula2.domain.port.out.*;

/**
 * Orquestra leitura, publicação confirmada e marcação dos eventos pendentes.
 *
 * <p>Quem chama: OutboxScheduler.tick.
 *
 * <p>Quem é chamado: RelayControl, OutboxRepository, OrderEventPublisher e TransactionBoundary.
 */
public class PublishPendingEventsUseCase {
  private final OutboxRepository outbox;
  private final OrderEventPublisher events;
  private final TransactionBoundary transactions;
  private final RelayControl control;

  public PublishPendingEventsUseCase(
      OutboxRepository outbox,
      OrderEventPublisher events,
      TransactionBoundary transactions,
      RelayControl control) {
    this.outbox = outbox;
    this.events = events;
    this.transactions = transactions;
    this.control = control;
  }

  public int execute() {
    if (control.paused()) return 0;
    // A porta coordena o commit; a implementação Spring fica em infra.
    return transactions.required(
        () -> {
          var batch = outbox.lockPending(10);
          for (var event : batch) {
            events.publish(event);
            // Somente após confirmação do broker; crash antes do commit pode causar reentrega.
            outbox.markPublished(event.id());
          }
          return batch.size();
        });
  }
}
