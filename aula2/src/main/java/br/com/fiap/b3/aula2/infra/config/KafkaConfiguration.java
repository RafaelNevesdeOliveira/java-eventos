package br.com.fiap.b3.aula2.infra.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Configura o tópico e a política de retentativa do consumidor.
 *
 * <p>Quem chama: Spring durante a inicialização da aula 2.
 *
 * <p>Quem é chamado: TopicBuilder e DefaultErrorHandler; mensagens inválidas podem bloquear a
 * partição.
 */
@Configuration
public class KafkaConfiguration {
  @Bean
  NewTopic topic() {
    return TopicBuilder.name(MessagingNames.ORDER_ACCEPTED_TOPIC).partitions(3).replicas(1).build();
  }

  @Bean
  CommonErrorHandler consumerErrorHandler() {
    // Neste laboratório, erro NÃO é descartado após poucas tentativas.
    // Mensagens venenosas bloqueiam a partição; DLQ e políticas de retry entram na aula 6.
    var handler = new DefaultErrorHandler(new FixedBackOff(2000, FixedBackOff.UNLIMITED_ATTEMPTS));
    handler.setAckAfterHandle(false);
    return handler;
  }
}
