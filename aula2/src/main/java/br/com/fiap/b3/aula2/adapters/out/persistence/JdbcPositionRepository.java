package br.com.fiap.b3.aula2.adapters.out.persistence;

import br.com.fiap.b3.aula2.application.port.out.PositionRepository;
import br.com.fiap.b3.ordens.domain.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPositionRepository implements PositionRepository {
  private final JdbcTemplate db;

  public JdbcPositionRepository(JdbcTemplate db) {
    this.db = db;
  }

  public void add(Order order) {
    db.update(
        "INSERT INTO posicao(conta_id,ativo,quantidade) VALUES (?,?,?) ON CONFLICT (conta_id,ativo)"
            + " DO UPDATE SET quantidade=posicao.quantidade+EXCLUDED.quantidade",
        order.contaId(),
        order.ativo(),
        order.quantidade());
  }
}
