package br.com.fiap.b3.aula2.infra.persistence;

import br.com.fiap.b3.aula2.domain.port.out.InboxRepository;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Deduplica a entrega usando a restrição única da Inbox.
 *
 * <p>Quem chama: ApplyOrderAcceptedUseCase através de InboxRepository.
 *
 * <p>Quem é chamado: JdbcTemplate com INSERT ON CONFLICT DO NOTHING.
 */
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
