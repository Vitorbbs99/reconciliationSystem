package com.reconciliationSystem.domain.repository;

import com.reconciliationSystem.domain.entity.Fatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FaturaRepository extends JpaRepository<Fatura, Long> {

  // Busca pelo indíce
  @Query("SELECT f FROM Fatura f WHERE f.txid = :txid")
  Optional<Fatura> findByTxidOtimizado(@Param("txid") String txid);
}
