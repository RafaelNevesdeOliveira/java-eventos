package br.com.fiap.b3.aula2.domain.port.out;

import java.util.function.Supplier;

/**
 * Define a execução atômica de um bloco sem dependência de Spring.
 *
 * <p>Quem chama: casos de criação, publicação e projeção da aula 2.
 *
 * <p>Quem é chamado: SpringTransactionBoundary.required; implementações de teste usam a mesma
 * porta.
 */
public interface TransactionBoundary {
  <T> T required(Supplier<T> work);
}
