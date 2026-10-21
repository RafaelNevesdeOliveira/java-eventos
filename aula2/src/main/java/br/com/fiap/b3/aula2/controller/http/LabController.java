package br.com.fiap.b3.aula2.controller.http;

import br.com.fiap.b3.aula2.domain.usecase.*;
import br.com.fiap.b3.aula2.presenter.LabPresenter;
import br.com.fiap.b3.aula2.presenter.LabPresenter.*;
import br.com.fiap.b3.aula2.presenter.contract.CloudEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Coordena os endpoints HTTP de ordens, consultas, relay e reenvio.
 *
 * <p>Quem chama: cliente HTTP nos endpoints /api.
 *
 * <p>Quem é chamado: casos de uso, RelayControl e LabPresenter; não executa SQL ou publicação
 * Kafka.
 */
@RestController
@RequestMapping("/api")
public class LabController {
  private final CreateOrderUseCase create;
  private final QueryLabUseCase query;
  private final RequeueEventUseCase requeue;
  private final RelayControl control;
  private final LabPresenter presenter;

  public LabController(
      CreateOrderUseCase create,
      QueryLabUseCase query,
      RequeueEventUseCase requeue,
      RelayControl control,
      LabPresenter presenter) {
    this.create = create;
    this.query = query;
    this.requeue = requeue;
    this.control = control;
    this.presenter = presenter;
  }

  /**
   * Recebe os campos de entrada HTTP com validação na fronteira.
   *
   * <p>Quem chama: Spring MVC ao desserializar POST /api/ordens.
   *
   * <p>Quem é chamado: controller encaminha valores simples ao caso de uso.
   */
  public record CreateOrderRequest(
      @NotBlank String contaId,
      @NotNull @Pattern(regexp = "[A-Z]{4}[0-9]{1,2}") String ativo,
      @Positive int quantidade) {}

  @PostMapping("/ordens")
  @ResponseStatus(HttpStatus.CREATED)
  public CloudEvent create(
      @Valid @RequestBody CreateOrderRequest request,
      @RequestParam(defaultValue = "false") boolean falhar) {
    return presenter.created(
        create.execute(request.contaId(), request.ativo(), request.quantidade(), falhar));
  }

  @GetMapping("/ordens")
  public List<OrderResponse> orders() {
    return presenter.orders(query.orders());
  }

  @GetMapping("/posicoes")
  public List<PositionResponse> positions() {
    return presenter.positions(query.positions());
  }

  @GetMapping("/outbox")
  public List<OutboxResponse> outbox() {
    return presenter.outbox(query.outbox());
  }

  @GetMapping("/lab/status")
  public StatusResponse status() {
    return presenter.status(query.status());
  }

  @PostMapping("/lab/relay/{acao}")
  public StatusResponse relay(@PathVariable String acao) {
    if (!List.of("pausar", "retomar").contains(acao))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use pausar ou retomar");
    control.pause(acao.equals("pausar"));
    return presenter.status(query.status());
  }

  @PostMapping("/lab/outbox/{id}/reenviar")
  public Map<String, Object> requeue(@PathVariable UUID id) {
    if (!requeue.execute(id))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado");
    return presenter.requeued(id);
  }

  @ExceptionHandler(SimulatedFailure.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public Map<String, String> simulatedFailure() {
    return presenter.simulatedFailure();
  }
}
