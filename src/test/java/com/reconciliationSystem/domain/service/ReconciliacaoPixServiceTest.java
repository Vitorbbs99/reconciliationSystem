package com.reconciliationSystem.domain.service;

import com.reconciliationSystem.domain.entity.Fatura;
import com.reconciliationSystem.domain.exception.TransacaoNaoEncontradaException;
import com.reconciliationSystem.domain.model.FaturaStatus;
import com.reconciliationSystem.domain.repository.FaturaRepository;
import com.reconciliationSystem.infrastructure.dto.TransacaoPixMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReconciliacaoPixServiceTest {

  @Mock
  private FaturaRepository faturaRepository;

  @InjectMocks
  private ReconciliacaoPixService reconciliacaoPixService;

  private Fatura faturaPendente;
  private String txidValido;

  @BeforeEach
  void setUp() {
    txidValido = "TXID-TESTE-123";
    faturaPendente = Fatura.builder()
      .id(1L)
      .txid(txidValido)
      .valorEsperado(new BigDecimal("100.00"))
      .status(FaturaStatus.PENDENTE)
      .dataCriacao(LocalDateTime.now())
      .build();
  }

  @Test
  void conciliarComSucesso () {
    TransacaoPixMessageDTO dto = new TransacaoPixMessageDTO(
      "PIX-01", txidValido, new BigDecimal("100.00"), LocalDateTime.now());

    when(faturaRepository.findByTxidOtimizado(txidValido)).thenReturn(Optional.of(faturaPendente));

    reconciliacaoPixService.processarReconciliacao(dto);

    assertEquals(FaturaStatus.CONCILIADO, faturaPendente.getStatus());
    assertEquals(new BigDecimal("100.00"), faturaPendente.getValorPago());
    assertNull(faturaPendente.getMotivoInconsistencia());
    assertNotNull(faturaPendente.getDataConciliacao());

    verify(faturaRepository, times(1)).save(faturaPendente);
  }

  @Test
  void marcarInconsistenteValorDivergente() {
    TransacaoPixMessageDTO dto = new TransacaoPixMessageDTO(
      "PIX-02", txidValido, new BigDecimal("90.00"), LocalDateTime.now());

    when(faturaRepository.findByTxidOtimizado(txidValido)).thenReturn(Optional.of(faturaPendente));

    reconciliacaoPixService.processarReconciliacao(dto);

    assertEquals(FaturaStatus.INCONSISTENTE, faturaPendente.getStatus());
    assertEquals(new BigDecimal("90.00"), faturaPendente.getValorPago());
    assertTrue(faturaPendente.getMotivoInconsistencia().contains("divergente"));

    verify(faturaRepository, times(1)).save(faturaPendente);
  }

  @Test
  void lancarExcecaoQuandoNaoEncontrado () {
    String txidInexistente = "TXID-INEXISTENTE";
    TransacaoPixMessageDTO dto = new TransacaoPixMessageDTO(
      "PIX-03", txidInexistente, new BigDecimal("100.00"), LocalDateTime.now());

    when(faturaRepository.findByTxidOtimizado(txidInexistente)).thenReturn(Optional.empty());

    assertThrows(TransacaoNaoEncontradaException.class, () ->
      reconciliacaoPixService.processarReconciliacao(dto)
    );

    verify(faturaRepository, never()).save(any());
  }

  @Test
  void ignorarSeEstiverConciliado() {
    faturaPendente.setStatus(FaturaStatus.CONCILIADO);
    TransacaoPixMessageDTO dto = new TransacaoPixMessageDTO(
      "PIX-04", txidValido, new BigDecimal("100.00"), LocalDateTime.now());

    when(faturaRepository.findByTxidOtimizado(txidValido)).thenReturn(Optional.of(faturaPendente));

    reconciliacaoPixService.processarReconciliacao(dto);

    verify(faturaRepository, never()).save(any());
  }

}
