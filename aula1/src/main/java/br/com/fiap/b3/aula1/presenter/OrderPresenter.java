package br.com.fiap.b3.aula1.presenter;

import java.util.UUID;

/**
 * Monta a resposta de comando aceito sem conhecer Spring ou RabbitMQ.
 *
 * <p>Quem chama: OrderController após SubmitOrderUseCase.
 *
 * <p>Quem é chamado: AcceptedOrderResponse com ordemId e status; não executa regra de negócio.
 */
public final class OrderPresenter {
  public AcceptedOrderResponse accepted(UUID orderId) {
    return new AcceptedOrderResponse(orderId, "COMANDO_ENFILEIRADO");
  }

  /**
   * Transporta os campos JSON da resposta HTTP 202 da aula 1.
   *
   * <p>Quem chama: OrderPresenter.accepted.
   *
   * <p>Quem é chamado: serializador HTTP ao retornar pelo controller.
   */
  public record AcceptedOrderResponse(UUID ordemId, String status) {}
}
