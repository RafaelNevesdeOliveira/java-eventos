package br.com.fiap.b3.aula2.application.usecase;

import br.com.fiap.b3.aula2.application.port.out.*;

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
