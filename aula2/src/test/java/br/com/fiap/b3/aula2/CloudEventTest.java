package br.com.fiap.b3.aula2;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fiap.b3.aula2.adapters.contract.CloudEvent;
import br.com.fiap.b3.ordens.domain.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class CloudEventTest {
  private CloudEvent message() {
    return CloudEvent.from(
        new OrderAccepted(
            UUID.randomUUID(),
            new Order(UUID.randomUUID(), "conta", "PETR4", 10),
            Instant.parse("2026-10-27T12:00:00Z")));
  }

  @Test
  void protocolRoundTripPreservesDomainIdentity() {
    var original = message();
    var json = JsonMapper.builder().build();
    var copy = json.readValue(json.writeValueAsString(original), CloudEvent.class);
    assertEquals(original, copy);
    assertEquals(original.toDomain(), copy.toDomain());
  }

  @Test
  void unknownVersionIsRejectedBeforeEnteringDomain() {
    var e = message();
    var future =
        new CloudEvent(
            e.specversion(),
            e.id(),
            e.source(),
            "br.com.b3.ordem.aceita.v2",
            e.subject(),
            e.time(),
            e.datacontenttype(),
            e.dataschema(),
            e.data());
    assertThrows(IllegalArgumentException.class, future::toDomain);
  }

  @Test
  void mismatchedSubjectIsRejected() {
    var e = message();
    var wrong =
        new CloudEvent(
            e.specversion(),
            e.id(),
            e.source(),
            e.type(),
            "ordens/outro",
            e.time(),
            e.datacontenttype(),
            e.dataschema(),
            e.data());
    assertThrows(IllegalArgumentException.class, wrong::toDomain);
  }
}
