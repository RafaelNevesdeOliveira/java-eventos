package br.com.fiap.b3.aula1.infra.messaging;

import br.com.fiap.b3.aula1.domain.port.out.OrderCommandPublisher;
import br.com.fiap.b3.aula1.presenter.contract.OrderCommand;
import br.com.fiap.b3.ordens.domain.Order;
import com.oracle.bmc.queue.QueueClient;
import com.oracle.bmc.queue.model.PutMessagesDetails;
import com.oracle.bmc.queue.model.PutMessagesDetailsEntry;
import com.oracle.bmc.queue.requests.PutMessagesRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Substitui o publicador RabbitMQ por PutMessages quando o perfil OCI está ativo.
 *
 * <p>Quem chama: SubmitOrderUseCase através da porta de domínio. Quem é chamado: contrato
 * OrderCommand e SDK Queue. HTTP 202 só retorna depois da confirmação do serviço.
 */
@Component
@Profile("oci")
public class OciOrderCommandPublisher implements OrderCommandPublisher {
  private final QueueClient queue;
  private final ObjectMapper json;
  private final String queueId;

  public OciOrderCommandPublisher(QueueClient queue, ObjectMapper json,
      @Value("${lab.oci.queue-id}") String queueId) {
    this.queue = queue;
    this.json = json;
    this.queueId = queueId;
  }

  public void publish(Order order) {
    var response = queue.putMessages(PutMessagesRequest.builder().queueId(queueId)
        .putMessagesDetails(PutMessagesDetails.builder().messages(List.of(
            PutMessagesDetailsEntry.builder()
                .content(json.writeValueAsString(OrderCommand.from(order))).build())).build())
        .build());
    if (response.getPutMessages() == null || response.getPutMessages().getMessages().size() != 1) {
      throw new IllegalStateException("OCI Queue não confirmou o comando");
    }
  }
}
