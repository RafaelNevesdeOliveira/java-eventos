package br.com.fiap.b3.aula2.application.usecase;

import br.com.fiap.b3.aula2.application.port.out.*;
import br.com.fiap.b3.ordens.domain.OrderAccepted;

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
