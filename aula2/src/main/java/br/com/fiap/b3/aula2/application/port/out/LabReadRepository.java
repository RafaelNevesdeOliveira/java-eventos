package br.com.fiap.b3.aula2.application.port.out;

import br.com.fiap.b3.aula2.application.model.ReadModels.*;
import java.util.List;

public interface LabReadRepository {
  List<OrderView> orders();

  List<PositionView> positions();

  List<OutboxView> outbox();

  Counters counters();
}
