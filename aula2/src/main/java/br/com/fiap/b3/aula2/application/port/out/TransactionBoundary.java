package br.com.fiap.b3.aula2.application.port.out;

import java.util.function.Supplier;

public interface TransactionBoundary {
  <T> T required(Supplier<T> work);
}
