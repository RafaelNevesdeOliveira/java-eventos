package br.com.fiap.b3.ordens.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OrderAccepted(UUID id, Order order, Instant occurredAt) {
  public OrderAccepted {
    Objects.requireNonNull(id);
    Objects.requireNonNull(order);
    Objects.requireNonNull(occurredAt);
  }
}
