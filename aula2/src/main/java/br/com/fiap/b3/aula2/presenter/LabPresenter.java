package br.com.fiap.b3.aula2.presenter;

import br.com.fiap.b3.aula2.domain.model.ReadModels.*;
import br.com.fiap.b3.aula2.presenter.contract.CloudEvent;
import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Converte os resultados dos casos de uso para o contrato HTTP da aula 2.
 *
 * <p>Quem chama: LabController após a execução ou consulta.
 *
 * <p>Quem é chamado: CloudEvent e DTOs de resposta; preserva campos snake_case esperados pelos
 * scripts.
 */
public final class LabPresenter {
  public CloudEvent created(OrderAccepted event) {
    return CloudEvent.from(event);
  }

  public List<OrderResponse> orders(List<OrderView> orders) {
    return orders.stream()
        .map(o -> new OrderResponse(o.id(), o.contaId(), o.ativo(), o.quantidade(), o.criadaEm()))
        .toList();
  }

  public List<PositionResponse> positions(List<PositionView> positions) {
    return positions.stream()
        .map(p -> new PositionResponse(p.contaId(), p.ativo(), p.quantidade()))
        .toList();
  }

  public List<OutboxResponse> outbox(List<OutboxView> outbox) {
    return outbox.stream()
        .map(
            o ->
                new OutboxResponse(
                    o.id(), o.aggregateId(), o.payload(), o.criadaEm(), o.publicadaEm()))
        .toList();
  }

  public StatusResponse status(Status status) {
    return new StatusResponse(
        status.relayPausado(),
        status.ordens(),
        status.outbox(),
        status.inbox(),
        status.pendentes());
  }

  public Map<String, Object> requeued(UUID eventId) {
    return Map.of("eventId", eventId, "status", "PENDENTE");
  }

  public Map<String, String> simulatedFailure() {
    return Map.of("erro", "Falha simulada: ordem e outbox revertidas");
  }

  /**
   * Define os nomes JSON da ordem retornada ao cliente.
   *
   * <p>Quem chama: LabPresenter.orders.
   *
   * <p>Quem é chamado: serializador HTTP; campos conta_id e criada_em preservam o contrato.
   */
  public record OrderResponse(
      UUID id, String conta_id, String ativo, int quantidade, Instant criada_em) {}

  /**
   * Define o formato JSON da projeção retornada ao cliente.
   *
   * <p>Quem chama: LabPresenter.positions.
   *
   * <p>Quem é chamado: serializador HTTP com conta_id, ativo e quantidade.
   */
  public record PositionResponse(String conta_id, String ativo, long quantidade) {}

  /**
   * Define o formato JSON de consulta da Outbox.
   *
   * <p>Quem chama: LabPresenter.outbox.
   *
   * <p>Quem é chamado: serializador HTTP; publicada_em pode ser null.
   */
  public record OutboxResponse(
      UUID id, UUID aggregate_id, String payload, Instant criada_em, Instant publicada_em) {}

  /**
   * Define o formato HTTP do status sem expor diretamente o modelo interno.
   *
   * <p>Quem chama: LabPresenter.status.
   *
   * <p>Quem é chamado: serializador HTTP com relayPausado e contadores.
   */
  public record StatusResponse(
      boolean relayPausado, long ordens, long outbox, long inbox, long pendentes) {}
}
