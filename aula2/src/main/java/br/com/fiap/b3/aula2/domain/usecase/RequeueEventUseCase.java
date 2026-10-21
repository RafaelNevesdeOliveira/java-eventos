package br.com.fiap.b3.aula2.domain.usecase;

import br.com.fiap.b3.aula2.domain.port.out.OutboxRepository;
import java.util.UUID;

/**
 * Solicita que o mesmo evento volte ao estado pendente para demonstrar reentrega.
 *
 * <p>Quem chama: LabController.requeue.
 *
 * <p>Quem é chamado: OutboxRepository.requeue; preserva a identidade do evento.
 */
public class RequeueEventUseCase {
  private final OutboxRepository outbox;

  public RequeueEventUseCase(OutboxRepository outbox) {
    this.outbox = outbox;
  }

  public boolean execute(UUID id) {
    return outbox.requeue(id);
  }
}
