package br.com.fiap.b3.aula2.domain.port.out;

import br.com.fiap.b3.aula2.domain.model.ReadModels.*;
import java.util.List;

/**
 * Define consultas tipadas sem dependência de JDBC ou nomes JSON.
 *
 * <p>Quem chama: QueryLabUseCase.
 *
 * <p>Quem é chamado: JdbcLabReadRepository, que retorna os tipos de ReadModels.
 */
public interface LabReadRepository {
  List<OrderView> orders();

  List<PositionView> positions();

  List<OutboxView> outbox();

  Counters counters();
}
