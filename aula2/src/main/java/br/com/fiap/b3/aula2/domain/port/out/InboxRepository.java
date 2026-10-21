package br.com.fiap.b3.aula2.domain.port.out;

import java.util.UUID;

/**
 * Define o registro único de consumidor, origem e evento.
 *
 * <p>Quem chama: ApplyOrderAcceptedUseCase.
 *
 * <p>Quem é chamado: JdbcInboxRepository; retorna false quando a entrega já foi aplicada.
 */
public interface InboxRepository {
  boolean register(String consumer, String source, UUID eventId);
}
