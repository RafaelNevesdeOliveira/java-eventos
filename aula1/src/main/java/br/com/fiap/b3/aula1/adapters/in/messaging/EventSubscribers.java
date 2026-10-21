package br.com.fiap.b3.aula1.adapters.in.messaging;

import br.com.fiap.b3.aula1.infrastructure.config.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventSubscribers {
  private static final Logger log = LoggerFactory.getLogger(EventSubscribers.class);

  @KafkaListener(topics = Topology.TOPIC, groupId = "aula1-risco")
  void risco(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
    registrar("RISCO", record);
  }

  @KafkaListener(topics = Topology.TOPIC, groupId = "aula1-auditoria")
  void auditoria(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
    registrar("AUDITORIA", record);
  }

  private void registrar(
      String grupo, org.apache.kafka.clients.consumer.ConsumerRecord<String, String> r) {
    log.info(
        "GRUPO={} particao={} offset={} chave={} evento={}",
        grupo,
        r.partition(),
        r.offset(),
        r.key(),
        r.value());
  }
}
