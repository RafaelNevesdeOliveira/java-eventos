package br.com.fiap.b3.aula1.application.port.out;

import br.com.fiap.b3.ordens.domain.Order;

public interface OrderCommandPublisher {
  void publish(Order order);
}
