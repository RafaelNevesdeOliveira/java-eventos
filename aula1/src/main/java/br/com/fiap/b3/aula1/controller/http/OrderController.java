package br.com.fiap.b3.aula1.controller.http;

import br.com.fiap.b3.aula1.domain.usecase.SubmitOrderUseCase;
import br.com.fiap.b3.aula1.presenter.OrderPresenter;
import br.com.fiap.b3.aula1.presenter.OrderPresenter.AcceptedOrderResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Recebe HTTP de criação, valida a entrada e coordena o caso de uso.
 *
 * <p>Quem chama: cliente HTTP em POST /api/ordens.
 *
 * <p>Quem é chamado: SubmitOrderUseCase e OrderPresenter; não acessa RabbitMQ diretamente.
 */
@RestController
@RequestMapping("/api/ordens")
public class OrderController {
  private final SubmitOrderUseCase submit;
  private final OrderPresenter presenter;

  public OrderController(SubmitOrderUseCase submit, OrderPresenter presenter) {
    this.submit = submit;
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

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public AcceptedOrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
    var orderId = submit.execute(request.contaId(), request.ativo(), request.quantidade());
    return presenter.accepted(orderId);
  }
}
