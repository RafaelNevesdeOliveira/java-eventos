package br.com.fiap.b3.aula2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.fiap.b3.aula2.controller.http.LabController;
import br.com.fiap.b3.aula2.domain.model.ReadModels.*;
import br.com.fiap.b3.aula2.domain.usecase.*;
import br.com.fiap.b3.aula2.presenter.LabPresenter;
import br.com.fiap.b3.ordens.domain.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Verifica a integração controller-presenter sem iniciar os serviços externos.
 *
 * <p>Quem chama: Maven Surefire ao executar mvn verify.
 *
 * <p>Quem é chamado: MockMvc, presenter real e casos de uso simulados para status e campos JSON.
 */
class HttpContractTest {
  private final CreateOrderUseCase create = mock(CreateOrderUseCase.class);
  private final QueryLabUseCase query = mock(QueryLabUseCase.class);
  private final RequeueEventUseCase requeue = mock(RequeueEventUseCase.class);
  private final RelayControl control = new RelayControl();
  private final MockMvc mvc =
      MockMvcBuilders.standaloneSetup(
              new LabController(create, query, requeue, control, new LabPresenter()))
          .build();
  private final UUID id = UUID.randomUUID();
  private final Instant time = Instant.parse("2026-10-21T12:00:00Z");

  @Test
  void createdResponseIsCloudEventWithHttp201() throws Exception {
    var event = new OrderAccepted(id, new Order(UUID.randomUUID(), "conta", "PETR4", 10), time);
    when(create.execute("conta", "PETR4", 10, false)).thenReturn(event);
    mvc.perform(
            post("/api/ordens")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contaId\":\"conta\",\"ativo\":\"PETR4\",\"quantidade\":10}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.specversion").value("1.0"))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.data.contaId").value("conta"))
        .andExpect(jsonPath("$.data.quantidade").value(10));
  }

  @Test
  void queriesPreserveExternalFieldNamesAndNullPublication() throws Exception {
    when(query.orders()).thenReturn(List.of(new OrderView(id, "conta", "PETR4", 10, time)));
    when(query.positions()).thenReturn(List.of(new PositionView("conta", "PETR4", 10)));
    when(query.outbox()).thenReturn(List.of(new OutboxView(id, id, "{}", time, null)));
    mvc.perform(get("/api/ordens"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].conta_id").value("conta"))
        .andExpect(jsonPath("$[0].criada_em").exists());
    mvc.perform(get("/api/posicoes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].conta_id").value("conta"))
        .andExpect(jsonPath("$[0].quantidade").value(10));
    mvc.perform(get("/api/outbox"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].aggregate_id").value(id.toString()))
        .andExpect(jsonPath("$[0].payload").value("{}"));
  }

  @Test
  void relayAndStatusKeepContractAndRejectUnknownAction() throws Exception {
    when(query.status()).thenReturn(new Status(true, 1, 1, 0, 1));
    mvc.perform(post("/api/lab/relay/pausar"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.relayPausado").value(true));
    assertTrue(control.paused());
    mvc.perform(post("/api/lab/relay/retomar")).andExpect(status().isOk());
    assertFalse(control.paused());
    mvc.perform(get("/api/lab/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pendentes").value(1));
    mvc.perform(post("/api/lab/relay/desconhecida")).andExpect(status().isBadRequest());
  }

  @Test
  void missingRequeueAndSimulatedFailureKeepErrorStatus() throws Exception {
    mvc.perform(post("/api/lab/outbox/" + id + "/reenviar")).andExpect(status().isNotFound());
    when(requeue.execute(id)).thenReturn(true);
    mvc.perform(post("/api/lab/outbox/" + id + "/reenviar"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.eventId").value(id.toString()))
        .andExpect(jsonPath("$.status").value("PENDENTE"));
    when(create.execute("conta", "PETR4", 10, true)).thenThrow(new SimulatedFailure());
    mvc.perform(
            post("/api/ordens?falhar=true")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contaId\":\"conta\",\"ativo\":\"PETR4\",\"quantidade\":10}"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.erro").value("Falha simulada: ordem e outbox revertidas"));
  }

  @Test
  void invalidHttpInputNeverReachesCreate() throws Exception {
    mvc.perform(
            post("/api/ordens")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contaId\":\"conta\",\"ativo\":\"PETR4\",\"quantidade\":0}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(create);
  }
}
