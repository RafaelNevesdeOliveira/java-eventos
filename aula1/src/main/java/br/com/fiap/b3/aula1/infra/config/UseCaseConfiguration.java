package br.com.fiap.b3.aula1.infra.config;

import br.com.fiap.b3.aula1.domain.port.out.*;
import br.com.fiap.b3.aula1.domain.usecase.*;
import br.com.fiap.b3.aula1.presenter.OrderPresenter;
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
  OrderPresenter presenter() {
    return new OrderPresenter();
  }

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
