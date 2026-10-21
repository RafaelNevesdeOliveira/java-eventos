package br.com.fiap.b3.aula2.application.port.out;

import br.com.fiap.b3.ordens.domain.Order;

public interface OrderRepository {
  void save(Order order);
}
