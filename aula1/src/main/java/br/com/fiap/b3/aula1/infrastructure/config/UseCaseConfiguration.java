package br.com.fiap.b3.aula1.infrastructure.config;

import br.com.fiap.b3.aula1.application.port.out.*;
import br.com.fiap.b3.aula1.application.usecase.*;
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
  SubmitOrderUseCase submitOrder(OrderCommandPublisher commands) {
    return new SubmitOrderUseCase(commands);
  }

  @Bean
  AcceptOrderUseCase acceptOrder(OrderEventPublisher events, Clock clock) {
    return new AcceptOrderUseCase(events, clock);
  }
}
