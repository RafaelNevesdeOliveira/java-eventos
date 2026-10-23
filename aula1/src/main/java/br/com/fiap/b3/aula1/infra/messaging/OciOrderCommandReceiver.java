package br.com.fiap.b3.aula1.infra.messaging;

import br.com.fiap.b3.aula1.domain.port.out.OrderCommandReceiver;
import br.com.fiap.b3.aula1.presenter.contract.OrderCommand;
import com.oracle.bmc.queue.QueueClient;
import com.oracle.bmc.queue.requests.DeleteMessageRequest;
import com.oracle.bmc.queue.requests.GetMessagesRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Traduz polling e confirmação OCI para a porta do domínio, sem expor o SDK ao controller.
 *
 * <p>Quem chama: OciOrderWorker. Quem é chamado: GetMessages e DeleteMessage. Uma mensagem por
 * vez evita ocultar um lote inteiro; sem confirmação, a entrega reaparece após a visibilidade.
 */
@Component
@Profile("oci")
public class OciOrderCommandReceiver implements OrderCommandReceiver {
  private final QueueClient queue;
  private final ObjectMapper json;
  private final String queueId;

  public OciOrderCommandReceiver(QueueClient queue, ObjectMapper json,
      @Value("${lab.oci.queue-id}") String queueId) {
    this.queue = queue;
    this.json = json;
    this.queueId = queueId;
  }

  public List<Delivery> receive() {
    return queue.getMessages(GetMessagesRequest.builder().queueId(queueId).limit(1)
            .timeoutInSeconds(5).visibilityInSeconds(120).build())
        .getGetMessages().getMessages().stream()
        .map(message -> new Delivery(message.getReceipt(),
            json.readValue(message.getContent(), OrderCommand.class).toDomain()))
        .toList();
  }

  public void acknowledge(String receipt) {
    queue.deleteMessage(DeleteMessageRequest.builder()
        .queueId(queueId).messageReceipt(receipt).build());
  }
}
