package br.com.fiap.b3.aula2.domain.usecase;

/** Falha deliberada para o exercício de atomicidade. */
/**
 * Sinaliza a falha deliberada do exercício de atomicidade.
 *
 * <p>Quem chama: CreateOrderUseCase quando falhar=true.
 *
 * <p>Quem é chamado: propagação por TransactionBoundary; LabController traduz para HTTP 503.
 */
public class SimulatedFailure extends RuntimeException {}
