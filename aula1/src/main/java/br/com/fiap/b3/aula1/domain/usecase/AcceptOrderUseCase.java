package br.com.fiap.b3.aula1.domain.usecase;

import br.com.fiap.b3.aula1.domain.port.out.OrderEventPublisher;
import br.com.fiap.b3.ordens.domain.Order;
import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.time.Clock;

/**
 * Transforma o comando consumido em fato de negócio e solicita sua publicação.
 *
 * <p>Quem chama: OrderWorker.execute.
 *
 * <p>Quem é chamado: OrderAccepted e a porta OrderEventPublisher; mantém o ID em reentregas.
 */
public class AcceptOrderUseCase {
  private final OrderEventPublisher events;
  private final Clock clock;

  public AcceptOrderUseCase(OrderEventPublisher events, Clock clock) {
    this.events = events;
    this.clock = clock;
  }

  public void execute(Order order) {
    events.publish(new OrderAccepted(order.id(), order, clock.instant()));
  }
}
