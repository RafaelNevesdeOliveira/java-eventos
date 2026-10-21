package br.com.fiap.b3.aula1.domain.port.out;

import br.com.fiap.b3.ordens.domain.Order;

/**
 * Define a capacidade de enfileirar uma ordem sem conhecer o broker.
 *
 * <p>Quem chama: SubmitOrderUseCase.
 *
 * <p>Quem é chamado: implementação RabbitOrderCommandPublisher injetada por infra.
 */
public interface OrderCommandPublisher {
  void publish(Order order);
}
