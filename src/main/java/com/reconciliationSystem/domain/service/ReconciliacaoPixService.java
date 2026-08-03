package com.reconciliationSystem.domain.service;

import com.reconciliationSystem.domain.entity.Fatura;
import com.reconciliationSystem.domain.exception.TransacaoNaoEncontradaException;
import com.reconciliationSystem.domain.model.FaturaStatus;
import com.reconciliationSystem.domain.repository.FaturaRepository;
import com.reconciliationSystem.infrastructure.dto.TransacaoPixMessageDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliacaoPixService {

  private static final Logger log = LoggerFactory.getLogger(ReconciliacaoPixService.class);
  private final FaturaRepository faturaRepository;

  public void processarReconciliacao(TransacaoPixMessageDTO dto) {
    // Busca
    Fatura fatura = faturaRepository.findByTxidOtimizado(dto.txid())
      .orElseThrow(() -> new TransacaoNaoEncontradaException(dto.txid()));

    //  Return se já processado
    if (FaturaStatus.CONCILIADO.equals(fatura.getStatus())) {
      log.info("Transação com TXID: {} já se encontra conciliada. Ignorando reprocessamento.", dto.txid());
      return;
    }

    // Aplica as regras
    atualizarDadosConciliacao(fatura, dto.valorPago());

    // Salva a fatura atualizada
    faturaRepository.save(fatura);
  }

  // Atualiza os dados da fatura
  private void atualizarDadosConciliacao(Fatura fatura, BigDecimal valorPago) {
    fatura.setValorPago(valorPago);
    fatura.setDataConciliacao(LocalDateTime.now());

    boolean eValorValido = fatura.getValorEsperado().compareTo(valorPago) == 0;

    if (eValorValido) {
      definirComoConciliado(fatura);
    } else {
      definirComoInconsistente(fatura, valorPago);
    }
  }

  // Atualiza como conciliado
  private void definirComoConciliado(Fatura fatura) {
    fatura.setStatus(FaturaStatus.CONCILIADO);
    fatura.setMotivoInconsistencia(null);
    log.info("Sucesso: Fatura ID {} conciliada para o TXID {}", fatura.getId(), fatura.getTxid());
  }

  // Lança motivo de inconsistência
  private void definirComoInconsistente(Fatura fatura, BigDecimal valorPago) {
    fatura.setStatus(FaturaStatus.INCONSISTENTE);

    String motivo = String.format("Valor pago (R$ %s) é divergente do valor esperado (R$ %s)",
      valorPago, fatura.getValorEsperado());

    fatura.setMotivoInconsistencia(motivo);
    log.warn("Inconsistência detectada na Fatura ID {}: {}", fatura.getId(), motivo);
  }

}
