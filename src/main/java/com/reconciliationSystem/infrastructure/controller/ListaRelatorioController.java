package com.reconciliationSystem.infrastructure.controller;

import com.reconciliationSystem.domain.service.ListaRelatorioService;
import com.reconciliationSystem.infrastructure.dto.RelatorioFaturasDTO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/relatorio")
@RequiredArgsConstructor
public class ListaRelatorioController {

  private final ListaRelatorioService listaRelatorioService;

  @GetMapping
  public ResponseEntity<List<RelatorioFaturasDTO>>listaRelatorio () {
    List<RelatorioFaturasDTO> listaRelatorio = listaRelatorioService.listaRelatorio();

    return ResponseEntity.ok(listaRelatorio);
  }

}
