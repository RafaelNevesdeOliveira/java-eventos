package br.com.fiap.b3.ordens.domain;

import java.util.Objects;
import java.util.UUID;

public record Order(UUID id, String contaId, String ativo, int quantidade) {
  public Order {
    Objects.requireNonNull(id, "ID da ordem obrigatório");
    if (contaId == null || contaId.isBlank())
      throw new IllegalArgumentException("Conta obrigatória");
    if (ativo == null || !ativo.matches("[A-Z]{4}[0-9]{1,2}"))
      throw new IllegalArgumentException("Ativo inválido");
    if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva");
  }
}
