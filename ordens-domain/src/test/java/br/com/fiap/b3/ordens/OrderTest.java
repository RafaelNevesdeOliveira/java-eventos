package br.com.fiap.b3.ordens;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fiap.b3.ordens.domain.Order;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderTest {
  @Test
  void domainRejectsInvalidOrderRegardlessOfTransport() {
    assertThrows(
        IllegalArgumentException.class, () -> new Order(UUID.randomUUID(), " ", "PETR4", 10));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Order(UUID.randomUUID(), "conta", "inválido", 10));
    assertThrows(
        IllegalArgumentException.class, () -> new Order(UUID.randomUUID(), "conta", "PETR4", 0));
    assertThrows(
        IllegalArgumentException.class, () -> new Order(UUID.randomUUID(), "conta", "PETR4", -1));
  }

  @Test
  void validOrderKeepsItsBusinessData() {
    var id = UUID.randomUUID();
    var order = new Order(id, "conta", "PETR4", 10);
    assertEquals(id, order.id());
    assertEquals(10, order.quantidade());
  }
}
