package br.com.fiap.b3.aula2.infrastructure.transaction;

import br.com.fiap.b3.aula2.application.port.out.TransactionBoundary;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
