package br.com.fiap.b3.aula2.adapters.in.web;

import br.com.fiap.b3.aula2.adapters.contract.CloudEvent;
import br.com.fiap.b3.aula2.application.model.ReadModels.*;
import br.com.fiap.b3.aula2.application.usecase.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class LabController {
  private final CreateOrderUseCase create;
  private final QueryLabUseCase query;
  private final RequeueEventUseCase requeue;
  private final RelayControl control;

  public LabController(
      CreateOrderUseCase create,
      QueryLabUseCase query,
      RequeueEventUseCase requeue,
      RelayControl control) {
    this.create = create;
    this.query = query;
    this.requeue = requeue;
    this.control = control;
  }

  public record CreateOrderRequest(
      @NotBlank String contaId,
      @NotNull @Pattern(regexp = "[A-Z]{4}[0-9]{1,2}") String ativo,
      @Positive int quantidade) {}

  // DTOs HTTP mantêm os nomes do contrato original; modelos internos usam camelCase.
  public record OrderResponse(
      UUID id, String conta_id, String ativo, int quantidade, Instant criada_em) {}

  public record PositionResponse(String conta_id, String ativo, long quantidade) {}

  public record OutboxResponse(
      UUID id, UUID aggregate_id, String payload, Instant criada_em, Instant publicada_em) {}

  @PostMapping("/ordens")
  @ResponseStatus(HttpStatus.CREATED)
  public CloudEvent create(
      @Valid @RequestBody CreateOrderRequest request,
      @RequestParam(defaultValue = "false") boolean falhar) {
    return CloudEvent.from(
        create.execute(request.contaId(), request.ativo(), request.quantidade(), falhar));
  }

  @GetMapping("/ordens")
  public List<OrderResponse> orders() {
    return query.orders().stream()
        .map(o -> new OrderResponse(o.id(), o.contaId(), o.ativo(), o.quantidade(), o.criadaEm()))
        .toList();
  }

  @GetMapping("/posicoes")
  public List<PositionResponse> positions() {
    return query.positions().stream()
        .map(p -> new PositionResponse(p.contaId(), p.ativo(), p.quantidade()))
        .toList();
  }

  @GetMapping("/outbox")
  public List<OutboxResponse> outbox() {
    return query.outbox().stream()
        .map(
            o ->
                new OutboxResponse(
                    o.id(), o.aggregateId(), o.payload(), o.criadaEm(), o.publicadaEm()))
        .toList();
  }

  @GetMapping("/lab/status")
  public Status status() {
    return query.status();
  }

  @PostMapping("/lab/relay/{acao}")
  public Status relay(@PathVariable String acao) {
    if (!List.of("pausar", "retomar").contains(acao))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use pausar ou retomar");
    control.pause(acao.equals("pausar"));
    return query.status();
  }

  @PostMapping("/lab/outbox/{id}/reenviar")
  public Map<String, Object> requeue(@PathVariable UUID id) {
    if (!requeue.execute(id))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado");
    return Map.of("eventId", id, "status", "PENDENTE");
  }

  @ExceptionHandler(SimulatedFailure.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public Map<String, String> simulatedFailure() {
    return Map.of("erro", "Falha simulada: ordem e outbox revertidas");
  }
}
