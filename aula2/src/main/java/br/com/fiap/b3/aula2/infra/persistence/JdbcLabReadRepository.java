package br.com.fiap.b3.aula2.infra.persistence;

import br.com.fiap.b3.aula2.domain.model.ReadModels.*;
import br.com.fiap.b3.aula2.domain.port.out.LabReadRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consulta ordens, posições, Outbox e contadores sem decidir o formato HTTP.
 *
 * <p>Quem chama: QueryLabUseCase através de LabReadRepository.
 *
 * <p>Quem é chamado: JdbcTemplate e construtores dos modelos de leitura internos.
 */
@Repository
public class JdbcLabReadRepository implements LabReadRepository {
  private final JdbcTemplate db;

  public JdbcLabReadRepository(JdbcTemplate db) {
    this.db = db;
  }

  public List<OrderView> orders() {
    return db.query(
        "SELECT * FROM ordens ORDER BY criada_em",
        (rs, n) ->
            new OrderView(
                rs.getObject("id", UUID.class),
                rs.getString("conta_id"),
                rs.getString("ativo"),
                rs.getInt("quantidade"),
                rs.getTimestamp("criada_em").toInstant()));
  }

  public List<PositionView> positions() {
    return db.query(
        "SELECT * FROM posicao ORDER BY conta_id,ativo",
        (rs, n) ->
            new PositionView(
                rs.getString("conta_id"), rs.getString("ativo"), rs.getLong("quantidade")));
  }

  public List<OutboxView> outbox() {
    return db.query(
        "SELECT * FROM outbox ORDER BY criada_em",
        (rs, n) -> {
          var published = rs.getTimestamp("publicada_em");
          return new OutboxView(
              rs.getObject("id", UUID.class),
              rs.getObject("aggregate_id", UUID.class),
              rs.getString("payload"),
              rs.getTimestamp("criada_em").toInstant(),
              published == null ? null : published.toInstant());
        });
  }

  public Counters counters() {
    return db.queryForObject(
        "SELECT (SELECT count(*) FROM ordens) AS ordens,(SELECT count(*) FROM outbox) AS"
            + " outbox,(SELECT count(*) FROM inbox) AS inbox,(SELECT count(*) FROM outbox WHERE"
            + " publicada_em IS NULL) AS pendentes",
        (rs, n) ->
            new Counters(
                rs.getLong("ordens"),
                rs.getLong("outbox"),
                rs.getLong("inbox"),
                rs.getLong("pendentes")));
  }
}
