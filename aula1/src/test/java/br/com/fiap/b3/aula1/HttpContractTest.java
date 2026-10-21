package br.com.fiap.b3.aula1;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.fiap.b3.aula1.controller.http.OrderController;
import br.com.fiap.b3.aula1.domain.usecase.SubmitOrderUseCase;
import br.com.fiap.b3.aula1.presenter.OrderPresenter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Verifica a integração controller-presenter sem iniciar os serviços externos.
 *
 * <p>Quem chama: Maven Surefire ao executar mvn verify.
 *
 * <p>Quem é chamado: MockMvc, presenter real e casos de uso simulados para status e campos JSON.
 */
class HttpContractTest {
  @Test
  void acceptedResponsePreservesIdAndHttp202() throws Exception {
    var submit = mock(SubmitOrderUseCase.class);
    var id = UUID.randomUUID();
    when(submit.execute("conta", "PETR4", 10)).thenReturn(id);
    var mvc =
        MockMvcBuilders.standaloneSetup(new OrderController(submit, new OrderPresenter())).build();
    mvc.perform(
            post("/api/ordens")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contaId\":\"conta\",\"ativo\":\"PETR4\",\"quantidade\":10}"))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.ordemId").value(id.toString()))
        .andExpect(jsonPath("$.status").value("COMANDO_ENFILEIRADO"));
    verify(submit).execute("conta", "PETR4", 10);
  }

  @Test
  void invalidHttpInputDoesNotInvokeUseCase() throws Exception {
    var submit = mock(SubmitOrderUseCase.class);
    var mvc =
        MockMvcBuilders.standaloneSetup(new OrderController(submit, new OrderPresenter())).build();
    mvc.perform(
            post("/api/ordens")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contaId\":\"conta\",\"ativo\":\"PETR4\",\"quantidade\":0}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(submit);
  }
}
