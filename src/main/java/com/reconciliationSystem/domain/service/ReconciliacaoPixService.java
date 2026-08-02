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

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliacaoPixService {

  private static final Logger log = LoggerFactory.getLogger(ReconciliacaoPixService.class);
  private final FaturaRepository faturaRepository;

  @Transactional
  public void processarReconciliacao(TransacaoPixMessageDTO transacaoPixMessageDTO) {

    log.debug("Iniciando conciliação da transação TXID: {}", transacaoPixMessageDTO.txid());

    // Busca pelo indíce
    Optional<Fatura> faturaOpt = faturaRepository.findByTxidOtimizado(transacaoPixMessageDTO.txid());

    // Se não existir uma fatura, lança um exceção
    if (faturaOpt.isEmpty()) {
      log.warn("Transação Pix não conciliada. TXID não encontrado no sistema: {}", transacaoPixMessageDTO.txid());
      throw new TransacaoNaoEncontradaException(transacaoPixMessageDTO.txid());
    }

    // Pega a entidade
    Fatura fatura = faturaOpt.get();

    //Verifica se a fatura já está conciliada
    if (FaturaStatus.CONCILIADO.equals(fatura.getStatus())) {
      log.info("Transação com TXID: {} já se encontra conciliada. Ignorando reprocessamento.", transacaoPixMessageDTO.txid());
      return;
    }

    // Atribui novos valores a entidade FATURA
    fatura.setValorPago(transacaoPixMessageDTO.valorPago());
    fatura.setDataConciliacao(LocalDateTime.now());

    // Verifica se o valor pago pelo cliente é igual a fatura existente
    if (fatura.getValorEsperado().compareTo(transacaoPixMessageDTO.valorPago()) == 0) {
      fatura.setStatus(FaturaStatus.CONCILIADO);
      fatura.setMotivoInconsistencia(null);
      log.info("Sucesso: Fatura ID {} conciliada para o TXID {}", transacaoPixMessageDTO.txid(), transacaoPixMessageDTO.txid());
    } else {
      fatura.setStatus(FaturaStatus.INCONSISTENTE);

      String motivo = String.format("Valor pago (R$ %s) é divergente do valor esperado (R$ %s)",
        transacaoPixMessageDTO.valorPago(), fatura.getValorEsperado());

      fatura.setMotivoInconsistencia(motivo);

      log.warn("Inconsistência detectada na Fatura ID {}: {}", fatura.getId(), motivo);
    }

    // Salva com novos valores
    faturaRepository.save(fatura);
  }

}
