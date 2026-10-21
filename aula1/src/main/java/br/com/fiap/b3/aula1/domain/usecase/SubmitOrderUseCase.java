package br.com.fiap.b3.aula1.domain.usecase;

import br.com.fiap.b3.aula1.domain.port.out.OrderCommandPublisher;
import br.com.fiap.b3.ordens.domain.Order;
import java.util.UUID;

/**
 * Cria e valida a ordem antes de solicitar o enfileiramento do comando.
 *
 * <p>Quem chama: OrderController.create.
 *
 * <p>Quem é chamado: Order e a porta OrderCommandPublisher; retorna o ID após a publicação.
 */
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
