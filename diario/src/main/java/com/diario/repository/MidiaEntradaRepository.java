package com.diario.repository;

import com.diario.model.MidiaEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MidiaEntradaRepository extends JpaRepository<MidiaEntrada, Long> {
    // Se eu precisar, meto um List<MidiaEntrada> findByEntradaDiarioId(Long entradaId);
}