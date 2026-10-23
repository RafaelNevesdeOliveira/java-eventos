package br.com.fiap.b3.aula1.domain.port.out;

import br.com.fiap.b3.ordens.domain.Order;
import java.util.List;

/**
 * Porta de entrada por polling: entrega ordens e permite confirmar apenas após processá-las.
 *
 * <p>Quem chama: OciOrderWorker. Quem implementa: OciOrderCommandReceiver, na camada infra.
 * Nenhum tipo do SDK OCI atravessa esta porta.
 */
public interface OrderCommandReceiver {
  List<Delivery> receive();

  void acknowledge(String receipt);

  /** Comando recebido e comprovante opaco usado para confirmar sua entrega. */
  record Delivery(String receipt, Order order) {}
}
