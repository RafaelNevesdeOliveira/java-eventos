package br.com.fiap.b3.aula1;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fiap.b3.aula1.domain.port.out.*;
import br.com.fiap.b3.aula1.domain.usecase.*;
import br.com.fiap.b3.ordens.domain.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

/**
 * Verifica as decisões dos casos de uso sem iniciar banco ou brokers.
 *
 * <p>Quem chama: Maven Surefire ao executar mvn verify.
 *
 * <p>Quem é chamado: casos de uso com portas falsas ou mocks e relógio fixo.
 */
class OrderUseCasesTest {
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-10-23T12:00:00Z"), ZoneOffset.UTC);

  @Test
  void submitPublishesValidatedCommand() {
    var published = new ArrayList<Order>();
    var submit = new SubmitOrderUseCase(published::add);
    var id = submit.execute("conta", "PETR4", 10);
    assertEquals(id, published.getFirst().id());
    assertEquals(10, published.getFirst().quantidade());
  }

  @Test
  void invalidInputNeverReachesPublisher() {
    var published = new ArrayList<Order>();
    assertThrows(
        IllegalArgumentException.class,
        () -> new SubmitOrderUseCase(published::add).execute("conta", "PETR4", 0));
    assertTrue(published.isEmpty());
  }

  @Test
  void commandRedeliveryKeepsStableEventIdentity() {
    var events = new ArrayList<OrderAccepted>();
    var order = new Order(UUID.randomUUID(), "conta", "PETR4", 10);
    var accept = new AcceptOrderUseCase(events::add, CLOCK);
    accept.execute(order);
    accept.execute(order);
    assertEquals(order.id(), events.getFirst().id());
    assertEquals(events.getFirst(), events.getLast());
  }
}
