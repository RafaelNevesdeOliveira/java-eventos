package br.com.fiap.b3.aula2.domain.usecase;

import br.com.fiap.b3.aula2.domain.port.out.*;
import br.com.fiap.b3.ordens.domain.OrderAccepted;

/**
 * Registra a Inbox e aplica a posição na mesma unidade transacional.
 *
 * <p>Quem chama: PositionConsumer.consume.
 *
 * <p>Quem é chamado: InboxRepository, PositionRepository e TransactionBoundary; duplicata não
 * altera posição.
 */
public class ApplyOrderAcceptedUseCase {
  private final InboxRepository inbox;
  private final PositionRepository positions;
  private final TransactionBoundary transactions;

  public ApplyOrderAcceptedUseCase(
      InboxRepository inbox, PositionRepository positions, TransactionBoundary transactions) {
    this.inbox = inbox;
    this.positions = positions;
    this.transactions = transactions;
  }

  public boolean execute(String source, OrderAccepted event) {
    // A porta coordena o commit; a implementação Spring fica em infra.
    return transactions.required(
        () -> {
          if (!inbox.register("posicao", source, event.id())) return false;
          // Inbox e efeito precisam do MESMO commit; offset é confirmado pelo adapter após o
          // retorno.
          positions.add(event.order());
          return true;
        });
  }
}
