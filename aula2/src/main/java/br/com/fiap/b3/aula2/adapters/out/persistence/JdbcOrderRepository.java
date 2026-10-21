package br.com.fiap.b3.aula2.adapters.out.persistence;

import br.com.fiap.b3.aula2.application.port.out.OrderRepository;
import br.com.fiap.b3.ordens.domain.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
