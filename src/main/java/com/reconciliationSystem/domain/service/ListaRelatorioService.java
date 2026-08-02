package com.reconciliationSystem.domain.service;

import com.reconciliationSystem.domain.entity.Fatura;
import com.reconciliationSystem.domain.repository.FaturaRepository;
import com.reconciliationSystem.infrastructure.dto.RelatorioFaturasDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListaRelatorioService {

  private final FaturaRepository faturaRepository;

  public List<RelatorioFaturasDTO> listaRelatorio () {
    return faturaRepository.findAll(Sort.by("id"))
      .stream()
      .map(relatorio -> new RelatorioFaturasDTO(
        relatorio.getId(),
        relatorio.getTxid(),
        relatorio.getStatus(),
        relatorio.getValorPago(),
        relatorio.getMotivoInconsistencia(),
        relatorio.getDataConciliacao()
      ))
      .toList();

  }
}
