package br.com.fiap.b3.aula1.application.usecase;

import br.com.fiap.b3.aula1.application.port.out.OrderCommandPublisher;
import br.com.fiap.b3.ordens.domain.Order;
import java.util.UUID;

public class SubmitOrderUseCase {
  private final OrderCommandPublisher commands;

  public SubmitOrderUseCase(OrderCommandPublisher commands) {
    this.commands = commands;
  }

  public UUID execute(String contaId, String ativo, int quantidade) {
    var order = new Order(UUID.randomUUID(), contaId, ativo, quantidade);
    commands.publish(order);
    return order.id();
  }
}
