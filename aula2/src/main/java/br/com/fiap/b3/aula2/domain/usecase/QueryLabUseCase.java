package br.com.fiap.b3.aula2.domain.usecase;

import br.com.fiap.b3.aula2.domain.model.ReadModels.*;
import br.com.fiap.b3.aula2.domain.port.out.LabReadRepository;
import java.util.List;

/**
 * Consulta os modelos internos de leitura e o estado local do relay.
 *
 * <p>Quem chama: LabController nos endpoints de consulta.
 *
 * <p>Quem é chamado: LabReadRepository e RelayControl; retorna modelos independentes do JSON.
 */
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
