package br.com.fiap.b3.aula2.domain.port.out;

import br.com.fiap.b3.ordens.domain.Order;

/**
 * Define a persistência de uma ordem sem expor SQL ao domínio.
 *
 * <p>Quem chama: CreateOrderUseCase.
 *
 * <p>Quem é chamado: JdbcOrderRepository na transação coordenada pelo caso de uso.
 */
public interface OrderRepository {
  void save(Order order);
}
