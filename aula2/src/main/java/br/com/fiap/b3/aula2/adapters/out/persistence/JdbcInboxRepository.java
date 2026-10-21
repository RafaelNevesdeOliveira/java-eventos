package br.com.fiap.b3.aula2.adapters.out.persistence;

import br.com.fiap.b3.aula2.application.port.out.InboxRepository;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcInboxRepository implements InboxRepository {
  private final JdbcTemplate db;

  public JdbcInboxRepository(JdbcTemplate db) {
    this.db = db;
  }

  public boolean register(String consumer, String source, UUID id) {
    return db.update(
            "INSERT INTO inbox(consumer_name,event_source,event_id) VALUES (?,?,?) ON CONFLICT DO"
                + " NOTHING",
            consumer,
            source,
            id)
        > 0;
  }
}
