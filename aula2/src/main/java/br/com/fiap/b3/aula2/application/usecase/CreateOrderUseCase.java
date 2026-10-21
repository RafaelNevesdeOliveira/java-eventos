package br.com.fiap.b3.aula2.application.usecase;

import br.com.fiap.b3.aula2.application.port.out.*;
import br.com.fiap.b3.ordens.domain.Order;
import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.time.Clock;
import java.util.UUID;

public class CreateOrderUseCase {
  private final OrderRepository orders;
  private final OutboxRepository outbox;
  private final TransactionBoundary transactions;
  private final Clock clock;

  public CreateOrderUseCase(
      OrderRepository orders,
      OutboxRepository outbox,
      TransactionBoundary transactions,
      Clock clock) {
    this.orders = orders;
    this.outbox = outbox;
    this.transactions = transactions;
    this.clock = clock;
  }

  public OrderAccepted execute(
      String contaId, String ativo, int quantidade, boolean simulateFailure) {
    var order = new Order(UUID.randomUUID(), contaId, ativo, quantidade);
    var event = new OrderAccepted(UUID.randomUUID(), order, clock.instant());
    return transactions.required(
        () -> {
          orders.save(order);
          outbox.append(event);
          if (simulateFailure) throw new SimulatedFailure();
          return event;
        });
  }
}
