package br.com.fiap.b3.aula2.infra.persistence;

import br.com.fiap.b3.aula2.domain.port.out.OrderRepository;
import br.com.fiap.b3.ordens.domain.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Grava a ordem na tabela ordens com os parâmetros recebidos do domínio.
 *
 * <p>Quem chama: CreateOrderUseCase através de OrderRepository.
 *
 * <p>Quem é chamado: JdbcTemplate; participa da transação externa sem abrir commit próprio.
 */
@Repository
public class JdbcOrderRepository implements OrderRepository {
  private final JdbcTemplate db;

  public JdbcOrderRepository(JdbcTemplate db) {
    this.db = db;
  }

  public void save(Order order) {
    db.update(
        "INSERT INTO ordens(id,conta_id,ativo,quantidade) VALUES (?,?,?,?)",
        order.id(),
        order.contaId(),
        order.ativo(),
        order.quantidade());
  }
}
