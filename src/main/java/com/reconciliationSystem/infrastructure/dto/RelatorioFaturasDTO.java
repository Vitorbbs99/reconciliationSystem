package com.reconciliationSystem.infrastructure.dto;

import com.reconciliationSystem.domain.model.FaturaStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RelatorioFaturasDTO (
  Long id,
  String txid,
  FaturaStatus status,
  BigDecimal valorPago,
  String motivoInconsistencia,
  LocalDateTime dataVencimento,
  LocalDateTime dataConciliacao
) {
}
