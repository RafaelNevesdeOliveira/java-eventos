package br.com.fiap.b3.aula2.presenter.contract;

import br.com.fiap.b3.ordens.domain.Order;
import br.com.fiap.b3.ordens.domain.OrderAccepted;
import java.time.Instant;
import java.util.UUID;

/**
 * Representa o envelope externo e valida a versão e a identidade do contrato.
 *
 * <p>Quem chama: LabPresenter, PositionConsumer, JdbcOutboxRepository e KafkaOrderEventPublisher.
 *
 * <p>Quem é chamado: Order e OrderAccepted em from/toDomain; não usa Spring nem acessa banco.
 */
public record CloudEvent(
    String specversion,
    UUID id,
    String source,
    String type,
    String subject,
    String time,
    String datacontenttype,
    String dataschema,
    OrdemData data) {
  public static final String SOURCE = "urn:b3:lab:ordens";
  public static final String TYPE = "br.com.b3.ordem.aceita.v1";

  public static CloudEvent from(OrderAccepted event) {
    var order = event.order();
    return new CloudEvent(
        "1.0",
        event.id(),
        SOURCE,
        TYPE,
        "ordens/" + order.id(),
        event.occurredAt().toString(),
        "application/json",
        "urn:b3:schema:ordem-aceita:1",
        new OrdemData(order.id(), order.contaId(), order.ativo(), order.quantidade()));
  }

  public OrderAccepted toDomain() {
    validar();
    return new OrderAccepted(
        id,
        new Order(data.ordemId(), data.contaId(), data.ativo(), data.quantidade()),
        Instant.parse(time));
  }

  public void validar() {
    if (!"1.0".equals(specversion)
        || id == null
        || !SOURCE.equals(source)
        || !TYPE.equals(type)
        || !"application/json".equals(datacontenttype)
        || !"urn:b3:schema:ordem-aceita:1".equals(dataschema)
        || data == null
        || data.ordemId() == null
        || !("ordens/" + data.ordemId()).equals(subject)
        || data.contaId() == null
        || data.contaId().isBlank()
        || data.ativo() == null
        || !data.ativo().matches("[A-Z]{4}[0-9]{1,2}")
        || data.quantidade() <= 0)
      throw new IllegalArgumentException("Contrato CloudEvents inválido ou tipo não suportado");
    Instant.parse(time);
  }

  /**
   * Transporta os dados da ordem dentro do envelope CloudEvents.
   *
   * <p>Quem chama: CloudEvent.from e desserialização do contrato.
   *
   * <p>Quem é chamado: dados imutáveis lidos pela conversão toDomain.
   */
  public record OrdemData(UUID ordemId, String contaId, String ativo, int quantidade) {}
}
