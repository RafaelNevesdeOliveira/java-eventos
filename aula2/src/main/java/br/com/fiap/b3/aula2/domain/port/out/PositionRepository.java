package br.com.fiap.b3.aula2.domain.port.out;

import br.com.fiap.b3.ordens.domain.Order;

/**
 * Define a atualização da projeção de posição.
 *
 * <p>Quem chama: ApplyOrderAcceptedUseCase.
 *
 * <p>Quem é chamado: JdbcPositionRepository dentro da mesma transação da Inbox.
 */
public interface PositionRepository {
  void add(Order order);
}
