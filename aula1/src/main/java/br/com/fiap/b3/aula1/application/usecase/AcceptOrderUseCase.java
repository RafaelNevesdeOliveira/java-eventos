package br.com.fiap.b3.aula1.application.usecase;

import br.com.fiap.b3.aula1.application.port.out.OrderEventPublisher;
import br.com.fiap.b3.ordens.domain.Order;
import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.time.Clock;

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
