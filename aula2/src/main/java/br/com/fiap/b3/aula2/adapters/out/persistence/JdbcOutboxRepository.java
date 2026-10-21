package br.com.fiap.b3.aula2.adapters.out.persistence;

import br.com.fiap.b3.aula2.adapters.contract.CloudEvent;
import br.com.fiap.b3.aula2.application.port.out.OutboxRepository;
import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
public class JdbcOutboxRepository implements OutboxRepository {
  private final JdbcTemplate db;
  private final ObjectMapper json;

  public JdbcOutboxRepository(JdbcTemplate db, ObjectMapper json) {
    this.db = db;
    this.json = json;
  }

  public void append(OrderAccepted event) {
    db.update(
        "INSERT INTO outbox(id,aggregate_id,payload) VALUES (?,?,?)",
        event.id(),
        event.order().id(),
        json.writeValueAsString(CloudEvent.from(event)));
  }

  public List<OrderAccepted> lockPending(int limit) {
    return db.query(
        "SELECT payload FROM outbox WHERE publicada_em IS NULL ORDER BY criada_em LIMIT ? FOR"
            + " UPDATE SKIP LOCKED",
        (row, n) -> json.readValue(row.getString("payload"), CloudEvent.class).toDomain(),
        limit);
  }

  public void markPublished(UUID id) {
    db.update("UPDATE outbox SET publicada_em=now() WHERE id=?", id);
  }

  public boolean requeue(UUID id) {
    return db.update("UPDATE outbox SET publicada_em=NULL WHERE id=?", id) > 0;
  }
}
