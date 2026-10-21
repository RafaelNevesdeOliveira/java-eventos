package br.com.fiap.b3.aula2.application.usecase;

import br.com.fiap.b3.aula2.application.port.out.OutboxRepository;
import java.util.UUID;

public class RequeueEventUseCase {
  private final OutboxRepository outbox;

  public RequeueEventUseCase(OutboxRepository outbox) {
    this.outbox = outbox;
  }

  public boolean execute(UUID id) {
    return outbox.requeue(id);
  }
}
