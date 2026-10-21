package br.com.fiap.b3.aula2.infra.persistence;

import br.com.fiap.b3.aula2.domain.port.out.PositionRepository;
import br.com.fiap.b3.ordens.domain.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Acumula quantidade por conta e ativo na projeção SQL.
 *
 * <p>Quem chama: ApplyOrderAcceptedUseCase através de PositionRepository.
 *
 * <p>Quem é chamado: JdbcTemplate com UPSERT; só aplica após o registro da Inbox.
 */
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
