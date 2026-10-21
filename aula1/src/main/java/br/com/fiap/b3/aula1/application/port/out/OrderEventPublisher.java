package br.com.fiap.b3.aula1.application.port.out;

import br.com.fiap.b3.ordens.domain.OrderAccepted;

public interface OrderEventPublisher {
  void publish(OrderAccepted event);
}
