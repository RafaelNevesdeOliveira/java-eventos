package br.com.fiap.b3.aula2.application.model;

import java.time.Instant;
import java.util.UUID;

public final class ReadModels {
  private ReadModels() {}

  public record OrderView(
      UUID id, String contaId, String ativo, int quantidade, Instant criadaEm) {}

  public record PositionView(String contaId, String ativo, long quantidade) {}

  public record OutboxView(
      UUID id, UUID aggregateId, String payload, Instant criadaEm, Instant publicadaEm) {}

  public record Counters(long ordens, long outbox, long inbox, long pendentes) {}

  public record Status(
      boolean relayPausado, long ordens, long outbox, long inbox, long pendentes) {}
}
