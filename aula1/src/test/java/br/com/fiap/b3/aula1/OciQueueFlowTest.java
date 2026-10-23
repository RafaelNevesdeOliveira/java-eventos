package br.com.fiap.b3.aula1;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.fiap.b3.aula1.controller.messaging.OciOrderWorker;
import br.com.fiap.b3.aula1.domain.port.out.OrderCommandReceiver;
import br.com.fiap.b3.aula1.domain.port.out.OrderEventPublisher;
import br.com.fiap.b3.aula1.domain.usecase.AcceptOrderUseCase;
import br.com.fiap.b3.aula1.infra.messaging.OciOrderCommandPublisher;
import br.com.fiap.b3.aula1.presenter.contract.OrderCommand;
import br.com.fiap.b3.ordens.domain.Order;
import com.oracle.bmc.queue.QueueClient;
import com.oracle.bmc.queue.model.PutMessage;
import com.oracle.bmc.queue.model.PutMessages;
import com.oracle.bmc.queue.requests.PutMessagesRequest;
import com.oracle.bmc.queue.responses.PutMessagesResponse;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

/**
 * Protege o ponto de confirmação Queue → domínio → Streaming sem precisar da nuvem no Maven.
 *
 * <p>Quem chama: Surefire. Quem é chamado: worker e publicador reais, com portas e SDK simulados.
 */
class OciQueueFlowTest {
  private final Order order = new Order(UUID.randomUUID(), "conta-oci", "PETR4", 10);

  @Test
  void confirmsQueueOnlyAfterStreamingAcceptedTheEvent() {
    var commands = mock(OrderCommandReceiver.class);
    var events = mock(OrderEventPublisher.class);
    when(commands.receive()).thenReturn(List.of(new OrderCommandReceiver.Delivery("receipt", order)));
    new OciOrderWorker(commands, new AcceptOrderUseCase(events, Clock.systemUTC())).execute();
    var sequence = inOrder(events, commands);
    sequence.verify(events).publish(any());
    sequence.verify(commands).acknowledge("receipt");
  }

  @Test
  void streamingFailureLeavesCommandUnacknowledgedForRedelivery() {
    var commands = mock(OrderCommandReceiver.class);
    var events = mock(OrderEventPublisher.class);
    when(commands.receive()).thenReturn(List.of(new OrderCommandReceiver.Delivery("receipt", order)));
    doThrow(new IllegalStateException("broker indisponivel")).when(events).publish(any());
    new OciOrderWorker(commands, new AcceptOrderUseCase(events, Clock.systemUTC())).execute();
    verify(commands, never()).acknowledge(anyString());
  }

  @Test
  void submitsTheSameOrderAndRequiresServiceConfirmation() {
    var queue = mock(QueueClient.class);
    var json = new ObjectMapper();
    when(queue.putMessages(any())).thenReturn(PutMessagesResponse.builder()
        .putMessages(PutMessages.builder().messages(List.of(PutMessage.builder().id(1L).build())).build())
        .build());
    new OciOrderCommandPublisher(queue, json, "queue-id").publish(order);
    var request = ArgumentCaptor.forClass(PutMessagesRequest.class);
    verify(queue).putMessages(request.capture());
    assertEquals("queue-id", request.getValue().getQueueId());
    var command = json.readValue(request.getValue().getPutMessagesDetails().getMessages().getFirst()
        .getContent(), OrderCommand.class);
    assertEquals(order, command.toDomain());
    when(queue.putMessages(any())).thenReturn(PutMessagesResponse.builder().build());
    assertThrows(IllegalStateException.class,
        () -> new OciOrderCommandPublisher(queue, json, "queue-id").publish(order));
  }
}
