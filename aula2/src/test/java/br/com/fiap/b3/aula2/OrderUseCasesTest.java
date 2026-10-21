package br.com.fiap.b3.aula2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.fiap.b3.aula2.application.port.out.*;
import br.com.fiap.b3.aula2.application.usecase.*;
import br.com.fiap.b3.ordens.domain.*;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class OrderUseCasesTest {
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-10-27T12:00:00Z"), ZoneOffset.UTC);

  private static OrderAccepted event() {
    return new OrderAccepted(
        UUID.randomUUID(), new Order(UUID.randomUUID(), "conta", "PETR4", 10), CLOCK.instant());
  }

  static class Boundary implements TransactionBoundary {
    boolean active, committed;

    public <T> T required(Supplier<T> work) {
      active = true;
      try {
        var result = work.get();
        committed = true;
        return result;
      } finally {
        active = false;
      }
    }
  }

  @Test
  void createPlacesOrderAndEventInsideSameBoundary() {
    var tx = new Boundary();
    var orders = new ArrayList<Order>();
    var events = new ArrayList<OrderAccepted>();
    var outbox = mock(OutboxRepository.class);
    doAnswer(
            invocation -> {
              assertTrue(tx.active);
              events.add(invocation.getArgument(0));
              return null;
            })
        .when(outbox)
        .append(any());
    var usecase =
        new CreateOrderUseCase(
            order -> {
              assertTrue(tx.active);
              orders.add(order);
            },
            outbox,
            tx,
            CLOCK);
    var accepted = usecase.execute("conta", "PETR4", 10, false);
    assertEquals(orders.getFirst(), accepted.order());
    assertEquals(accepted, events.getFirst());
    assertEquals(CLOCK.instant(), accepted.occurredAt());
    assertTrue(tx.committed);
  }

  @Test
  void simulatedFailureEscapesTransactionAndPreventsCommit() {
    var tx = new Boundary();
    var orders = mock(OrderRepository.class);
    var outbox = mock(OutboxRepository.class);
    assertThrows(
        SimulatedFailure.class,
        () ->
            new CreateOrderUseCase(orders, outbox, tx, CLOCK).execute("conta", "PETR4", 10, true));
    verify(orders).save(any());
    verify(outbox).append(any());
    assertFalse(tx.committed);
  }

  @Test
  void duplicateDoesNotChangeProjection() {
    var tx = new Boundary();
    var inbox = mock(InboxRepository.class);
    var positions = mock(PositionRepository.class);
    var e = event();
    when(inbox.register("posicao", "source", e.id())).thenReturn(false);
    assertFalse(new ApplyOrderAcceptedUseCase(inbox, positions, tx).execute("source", e));
    verifyNoInteractions(positions);
  }

  @Test
  void firstDeliveryRegistersInboxAndChangesPositionWithinTransaction() {
    var tx = new Boundary();
    var e = event();
    var inbox = mock(InboxRepository.class);
    var positions = mock(PositionRepository.class);
    when(inbox.register("posicao", "source", e.id()))
        .thenAnswer(
            i -> {
              assertTrue(tx.active);
              return true;
            });
    doAnswer(
            i -> {
              assertTrue(tx.active);
              return null;
            })
        .when(positions)
        .add(e.order());
    assertTrue(new ApplyOrderAcceptedUseCase(inbox, positions, tx).execute("source", e));
    var ordered = inOrder(inbox, positions);
    ordered.verify(inbox).register("posicao", "source", e.id());
    ordered.verify(positions).add(e.order());
    assertTrue(tx.committed);
  }

  @Test
  void brokerFailureDoesNotMarkOutboxAsPublished() {
    var tx = new Boundary();
    var outbox = mock(OutboxRepository.class);
    var events = mock(OrderEventPublisher.class);
    var e = event();
    when(outbox.lockPending(10)).thenReturn(List.of(e));
    doThrow(new IllegalStateException("offline")).when(events).publish(e);
    assertThrows(
        IllegalStateException.class,
        () -> new PublishPendingEventsUseCase(outbox, events, tx, new RelayControl()).execute());
    verify(outbox, never()).markPublished(any());
    assertFalse(tx.committed);
  }

  @Test
  void brokerConfirmationPrecedesPublishedMark() {
    var tx = new Boundary();
    var outbox = mock(OutboxRepository.class);
    var events = mock(OrderEventPublisher.class);
    var e = event();
    when(outbox.lockPending(10)).thenReturn(List.of(e));
    assertEquals(
        1, new PublishPendingEventsUseCase(outbox, events, tx, new RelayControl()).execute());
    var ordered = inOrder(outbox, events);
    ordered.verify(outbox).lockPending(10);
    ordered.verify(events).publish(e);
    ordered.verify(outbox).markPublished(e.id());
    assertTrue(tx.committed);
  }

  @Test
  void pausedRelayDoesNotAccessInfrastructure() {
    var outbox = mock(OutboxRepository.class);
    var events = mock(OrderEventPublisher.class);
    var control = new RelayControl();
    control.pause(true);
    assertEquals(
        0, new PublishPendingEventsUseCase(outbox, events, new Boundary(), control).execute());
    verifyNoInteractions(outbox, events);
  }
}
