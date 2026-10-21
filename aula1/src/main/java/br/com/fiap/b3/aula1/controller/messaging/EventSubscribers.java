package br.com.fiap.b3.aula1.controller.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Demonstra assinaturas Kafka independentes para Risco e Auditoria.
 *
 * <p>Quem chama: listeners Kafka dos grupos aula1-risco e aula1-auditoria.
 *
 * <p>Quem é chamado: logger com grupo, partição, offset, chave e evento; não altera o domínio.
 */
@Component
public class EventSubscribers {
  private static final Logger log = LoggerFactory.getLogger(EventSubscribers.class);

  @KafkaListener(topics = "aula1.ordens.aceitas.v1", groupId = "aula1-risco")
  void risco(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
    registrar("RISCO", record);
  }

  @KafkaListener(topics = "aula1.ordens.aceitas.v1", groupId = "aula1-auditoria")
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
