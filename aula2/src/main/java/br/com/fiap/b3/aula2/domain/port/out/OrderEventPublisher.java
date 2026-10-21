package br.com.fiap.b3.aula2.domain.port.out;

import br.com.fiap.b3.ordens.domain.OrderAccepted;

/**
 * Define a capacidade de publicar o evento aceito sem acoplar o domínio ao Kafka.
 *
 * <p>Quem chama: AcceptOrderUseCase ou PublishPendingEventsUseCase da respectiva aula.
 *
 * <p>Quem é chamado: implementação KafkaOrderEventPublisher injetada por infra.
 */
public interface OrderEventPublisher {
  void publish(OrderAccepted event);
}
