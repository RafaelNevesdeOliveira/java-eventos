package br.com.fiap.b3.aula2.domain.usecase;

import br.com.fiap.b3.aula2.domain.port.out.*;
import br.com.fiap.b3.ordens.domain.Order;
import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.time.Clock;
import java.util.UUID;

/**
 * Define a gravação atômica de ordem e evento na Outbox.
 *
 * <p>Quem chama: LabController.create.
 *
 * <p>Quem é chamado: OrderRepository e OutboxRepository dentro de TransactionBoundary; falha
 * simulada impede commit.
 */
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
    // A porta coordena o commit; a implementação Spring fica em infra.
    return transactions.required(
        () -> {
          // Ordem e fato ficam atômicos: não pode existir evento sem a ordem correspondente.
          orders.save(order);
          outbox.append(event);
          if (simulateFailure) throw new SimulatedFailure();
          return event;
        });
  }
}
