package br.com.fiap.b3.aula1.presenter.contract;

import br.com.fiap.b3.ordens.domain.Order;
import java.util.UUID;

/**
 * Representa o comando na fronteira de mensageria e converte a ordem.
 *
 * <p>Quem chama: RabbitOrderCommandPublisher e OrderWorker.
 *
 * <p>Quem é chamado: Order em from/toDomain; não envia mensagens.
 */
public record OrderCommand(UUID ordemId, String contaId, String ativo, int quantidade) {
  public static OrderCommand from(Order order) {
    return new OrderCommand(order.id(), order.contaId(), order.ativo(), order.quantidade());
  }

  public Order toDomain() {
    return new Order(ordemId, contaId, ativo, quantidade);
  }
}
