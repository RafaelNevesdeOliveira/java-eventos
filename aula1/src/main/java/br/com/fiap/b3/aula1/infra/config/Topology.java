package br.com.fiap.b3.aula1.infra.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Profile;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara a fila durável e o tópico Kafka com três partições.
 *
 * <p>Quem chama: Spring durante a inicialização da aula 1.
 *
 * <p>Quem é chamado: beans Queue e NewTopic; os listeners usam os mesmos nomes de destino.
 */
@Configuration
@Profile("!oci")
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
