package br.com.fiap.b3.aula2.application.usecase;

import java.util.concurrent.atomic.AtomicBoolean;

/** Controle didático desta instância; não é uma configuração distribuída. */
public class RelayControl {
  private final AtomicBoolean paused = new AtomicBoolean();

  public void pause(boolean value) {
    paused.set(value);
  }

  public boolean paused() {
    return paused.get();
  }
}
