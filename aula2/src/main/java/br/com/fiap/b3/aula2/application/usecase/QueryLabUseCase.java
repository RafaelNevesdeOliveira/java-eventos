package br.com.fiap.b3.aula2.application.usecase;

import br.com.fiap.b3.aula2.application.model.ReadModels.*;
import br.com.fiap.b3.aula2.application.port.out.LabReadRepository;
import java.util.List;

public class QueryLabUseCase {
  private final LabReadRepository reads;
  private final RelayControl control;

  public QueryLabUseCase(LabReadRepository reads, RelayControl control) {
    this.reads = reads;
    this.control = control;
  }

  public List<OrderView> orders() {
    return reads.orders();
  }

  public List<PositionView> positions() {
    return reads.positions();
  }

  public List<OutboxView> outbox() {
    return reads.outbox();
  }

  public Status status() {
    var c = reads.counters();
    return new Status(control.paused(), c.ordens(), c.outbox(), c.inbox(), c.pendentes());
  }
}
