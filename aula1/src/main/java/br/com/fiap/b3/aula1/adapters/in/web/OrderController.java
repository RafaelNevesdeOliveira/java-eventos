package br.com.fiap.b3.aula1.adapters.in.web;

import br.com.fiap.b3.aula1.application.usecase.SubmitOrderUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ordens")
public class OrderController {
  private final SubmitOrderUseCase submit;

  public OrderController(SubmitOrderUseCase submit) {
    this.submit = submit;
  }

  public record CreateOrderRequest(
      @NotBlank String contaId,
      @NotNull @Pattern(regexp = "[A-Z]{4}[0-9]{1,2}") String ativo,
      @Positive int quantidade) {}

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, Object> create(@Valid @RequestBody CreateOrderRequest request) {
    return Map.of(
        "ordemId",
        submit.execute(request.contaId(), request.ativo(), request.quantidade()),
        "status",
        "COMANDO_ENFILEIRADO");
  }
}
