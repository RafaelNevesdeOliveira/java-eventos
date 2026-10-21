package br.com.fiap.b3.ordens.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa o fato de que uma ordem foi aceita, com identidade e instante.
 *
 * <p>Quem chama: AcceptOrderUseCase, CreateOrderUseCase e CloudEvent.toDomain.
 *
 * <p>Quem é chamado: Order; não publica mensagens por conta própria.
 */
public record OrderAccepted(UUID id, Order order, Instant occurredAt) {
  public OrderAccepted {
    Objects.requireNonNull(id);
    Objects.requireNonNull(order);
    Objects.requireNonNull(occurredAt);
  }
}
