package br.com.fiap.b3.aula2.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Agrupa os modelos internos de consulta, sem impor nomes de campos HTTP.
 *
 * <p>Quem chama: LabReadRepository, QueryLabUseCase e LabPresenter.
 *
 * <p>Quem é chamado: records OrderView, PositionView, OutboxView, Counters e Status.
 */
public final class ReadModels {
  private ReadModels() {}

  /**
   * Representa uma ordem consultada com nomes internos em camelCase.
   *
   * <p>Quem chama: JdbcLabReadRepository.
   *
   * <p>Quem é chamado: QueryLabUseCase e LabPresenter.orders.
   */
  public record OrderView(
      UUID id, String contaId, String ativo, int quantidade, Instant criadaEm) {}

  /**
   * Representa a posição consultada por conta e ativo.
   *
   * <p>Quem chama: JdbcLabReadRepository.
   *
   * <p>Quem é chamado: QueryLabUseCase e LabPresenter.positions.
   */
  public record PositionView(String contaId, String ativo, long quantidade) {}

  /**
   * Representa o estado de um evento persistido, incluindo publicação opcional.
   *
   * <p>Quem chama: JdbcLabReadRepository.
   *
   * <p>Quem é chamado: QueryLabUseCase e LabPresenter.outbox.
   */
  public record OutboxView(
      UUID id, UUID aggregateId, String payload, Instant criadaEm, Instant publicadaEm) {}

  /**
   * Transporta os totais SQL usados para compor o status do laboratório.
   *
   * <p>Quem chama: JdbcLabReadRepository.counters.
   *
   * <p>Quem é chamado: QueryLabUseCase.status.
   */
  public record Counters(long ordens, long outbox, long inbox, long pendentes) {}

  /**
   * Representa o status interno com os contadores e a pausa local do relay.
   *
   * <p>Quem chama: QueryLabUseCase.status.
   *
   * <p>Quem é chamado: LabPresenter.status; não depende do transporte HTTP.
   */
  public record Status(
      boolean relayPausado, long ordens, long outbox, long inbox, long pendentes) {}
}
