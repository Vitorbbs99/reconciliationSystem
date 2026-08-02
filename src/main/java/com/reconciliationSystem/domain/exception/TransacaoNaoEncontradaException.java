package com.reconciliationSystem.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TransacaoNaoEncontradaException extends RuntimeException {
  public TransacaoNaoEncontradaException(String id) {
    super("Fatura não encontrada para o TXID: " + id);
  }
}
