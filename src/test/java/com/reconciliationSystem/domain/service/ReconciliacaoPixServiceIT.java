package com.reconciliationSystem.domain.service;

import com.reconciliationSystem.domain.entity.Fatura;
import com.reconciliationSystem.domain.model.FaturaStatus;
import com.reconciliationSystem.domain.repository.FaturaRepository;
import com.reconciliationSystem.infrastructure.dto.TransacaoPixMessageDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReconciliacaoPixServiceIT {

  @Autowired
  private ReconciliacaoPixService reconciliacaoPixService;

  @Autowired
  private FaturaRepository faturaRepository;

  private String txid;

  @BeforeEach
  void setUp() {
    faturaRepository.deleteAll();

    txid = "TXID-INTEGRACAO-999";;
    Fatura fatura = Fatura.builder()
      .txid(txid)
      .valorEsperado(new BigDecimal("250.00"))
      .status(FaturaStatus.PENDENTE)
      .build();

    faturaRepository.save(fatura);
  }

  @Test
  void persistirConciliacaoNoBanco() {// Arrange
    TransacaoPixMessageDTO dto = new TransacaoPixMessageDTO(
      "PIX-INT-01", txid, new BigDecimal("250.00"), LocalDateTime.now());

    reconciliacaoPixService.processarReconciliacao(dto);

    Fatura faturaAtualizada = faturaRepository.findByTxidOtimizado(txid).orElseThrow();

    assertEquals(FaturaStatus.CONCILIADO, faturaAtualizada.getStatus());
    assertEquals(0, new BigDecimal("250.00").compareTo(faturaAtualizada.getValorPago()));
    assertNotNull(faturaAtualizada.getDataConciliacao());
  }
}
