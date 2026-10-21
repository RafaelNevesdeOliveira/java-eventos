package br.com.fiap.b3.aula2.infra.config;

/**
 * Centraliza o nome técnico do tópico usado pela infraestrutura da aula 2.
 *
 * <p>Quem chama: KafkaConfiguration e KafkaOrderEventPublisher.
 *
 * <p>Quem é chamado: nenhuma chamada de negócio; o listener usa o mesmo nome de tópico.
 */
public final class MessagingNames {
  public static final String ORDER_ACCEPTED_TOPIC = "aula2.ordens.aceitas.v1";

  private MessagingNames() {}
}
