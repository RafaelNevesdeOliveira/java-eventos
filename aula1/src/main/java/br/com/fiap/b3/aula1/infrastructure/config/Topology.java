package br.com.fiap.b3.aula1.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class Topology {
  public static final String QUEUE = "aula1.ordens.comandos";
  public static final String TOPIC = "aula1.ordens.aceitas.v1";

  @Bean
  Queue ordensQueue() {
    return new Queue(QUEUE, true);
  }

  @Bean
  NewTopic ordensTopic() {
    return TopicBuilder.name(TOPIC).partitions(3).replicas(1).build();
  }
}
