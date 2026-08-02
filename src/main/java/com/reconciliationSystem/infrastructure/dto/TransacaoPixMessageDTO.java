package com.reconciliationSystem.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoPixMessageDTO(

  @NotBlank(message = "O ID da transação Pix é obrigatório")
  @JsonProperty("idTransacaoPix")
  String idTransacaoPix,

  @NotBlank(message = "O TXID é obrigatório para a conciliação")
  @JsonProperty("txid")
  String txid,

  @NotNull(message = "O valor pago é obrigatório")
  @Positive(message = "O valor pago deve ser maior que zero")
  @JsonProperty("valorPago")
  BigDecimal valorPago,

  @NotNull(message = "A data do pagamento é obrigatória")
  @JsonProperty("dataPagamento")
  LocalDateTime dataPagamento

) {
}
