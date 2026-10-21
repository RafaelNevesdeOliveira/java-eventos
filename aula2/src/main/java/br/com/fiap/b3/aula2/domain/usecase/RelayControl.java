package br.com.fiap.b3.aula2.domain.usecase;

import java.util.concurrent.atomic.AtomicBoolean;

/** Controle didático desta instância; não é uma configuração distribuída. */
/**
 * Guarda a pausa do relay nesta instância para o experimento didático.
 *
 * <p>Quem chama: LabController e casos PublishPendingEventsUseCase e QueryLabUseCase.
 *
 * <p>Quem é chamado: AtomicBoolean; não é configuração distribuída nem persistida.
 */
public class RelayControl {
  private final AtomicBoolean paused = new AtomicBoolean();

  public void pause(boolean value) {
    paused.set(value);
  }

  public boolean paused() {
    return paused.get();
  }
}
