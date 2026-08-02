package com.reconciliationSystem.domain.entity;

import com.reconciliationSystem.domain.model.FaturaStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "faturas", indexes = {
  @Index(name = "idx_fatura_txid", columnList = "txid")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Fatura {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Identificador do Pix
  @Column(nullable = false, unique = true, length = 100)
  private String txid;

  @Column(name = "valor_esperado", nullable = false, precision = 19, scale = 2)
  private BigDecimal valorEsperado;

  @Column(name = "valor_pago", precision = 19, scale = 2)
  private BigDecimal valorPago;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private FaturaStatus status;

  @Column(name = "motivo_inconsistencia")
  private String motivoInconsistencia;

  @Column(name = "data_criacao", nullable = false, updatable = false)
  private LocalDateTime dataCriacao;

  @Column(name = "data_conciliacao")
  private LocalDateTime dataConciliacao;

  @PrePersist
  public void prePersist() {
    this.dataCriacao = LocalDateTime.now();
    if (this.status == null) {
      this.status = FaturaStatus.PENDENTE;
    }
  }
}
