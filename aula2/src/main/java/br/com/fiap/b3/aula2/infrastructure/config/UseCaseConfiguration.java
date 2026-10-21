package br.com.fiap.b3.aula2.infrastructure.config;

import br.com.fiap.b3.aula2.application.port.out.*;
import br.com.fiap.b3.aula2.application.usecase.*;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfiguration {
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
