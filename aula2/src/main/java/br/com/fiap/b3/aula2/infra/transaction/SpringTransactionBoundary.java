package br.com.fiap.b3.aula2.infra.transaction;

import br.com.fiap.b3.aula2.domain.port.out.TransactionBoundary;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Executa a porta transacional usando o gerenciador Spring.
 *
 * <p>Quem chama: casos de uso através de TransactionBoundary.
 *
 * <p>Quem é chamado: TransactionTemplate; erro do bloco provoca rollback do banco.
 */
@Component
public class SpringTransactionBoundary implements TransactionBoundary {
  private final TransactionTemplate template;

  public SpringTransactionBoundary(PlatformTransactionManager manager) {
    this.template = new TransactionTemplate(manager);
  }

  public <T> T required(Supplier<T> work) {
    return template.execute(status -> work.get());
  }
}
