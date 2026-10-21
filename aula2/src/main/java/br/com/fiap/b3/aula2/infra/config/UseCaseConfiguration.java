package br.com.fiap.b3.aula2.infra.config;

import br.com.fiap.b3.aula2.domain.port.out.*;
import br.com.fiap.b3.aula2.domain.usecase.*;
import br.com.fiap.b3.aula2.presenter.LabPresenter;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Compõe os casos de uso, o relógio e o presenter por injeção de construtor.
 *
 * <p>Quem chama: Spring durante a inicialização da aplicação.
 *
 * <p>Quem é chamado: construtores do domínio e do presenter, conectando portas às implementações
 * infra.
 */
@Configuration
public class UseCaseConfiguration {
  @Bean
  LabPresenter presenter() {
    return new LabPresenter();
  }

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  RelayControl relayControl() {
    return new RelayControl();
  }

  @Bean
  CreateOrderUseCase createOrder(
      OrderRepository orders,
      OutboxRepository outbox,
      TransactionBoundary transactions,
      Clock clock) {
    return new CreateOrderUseCase(orders, outbox, transactions, clock);
  }

  @Bean
  PublishPendingEventsUseCase publishPending(
      OutboxRepository outbox,
      OrderEventPublisher events,
      TransactionBoundary transactions,
      RelayControl control) {
    return new PublishPendingEventsUseCase(outbox, events, transactions, control);
  }

  @Bean
  ApplyOrderAcceptedUseCase applyOrder(
      InboxRepository inbox, PositionRepository positions, TransactionBoundary transactions) {
    return new ApplyOrderAcceptedUseCase(inbox, positions, transactions);
  }

  @Bean
  QueryLabUseCase queryLab(LabReadRepository reads, RelayControl control) {
    return new QueryLabUseCase(reads, control);
  }

  @Bean
  RequeueEventUseCase requeueEvent(OutboxRepository outbox) {
    return new RequeueEventUseCase(outbox);
  }
}
