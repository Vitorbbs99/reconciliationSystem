package com.reconciliationSystem.infrastructure.listener;

import com.reconciliationSystem.domain.service.ReconciliacaoPixService;
import com.reconciliationSystem.infrastructure.dto.TransacaoPixMessageDTO;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionListener.class);
    private final ReconciliacaoPixService reconciliacaoPixService;

    @SqsListener(value ="${aws.sqs.queue-name}")
    public void listen(TransacaoPixMessageDTO message) {

        log.info("[THREAD {}] Reconciliando transação: {}",
                Thread.currentThread().getName(), message.txid());

        // Salva a transação no banco
      reconciliacaoPixService.processarReconciliacao(message);
    }

}
