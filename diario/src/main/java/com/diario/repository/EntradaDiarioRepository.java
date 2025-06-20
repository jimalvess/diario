package com.diario.repository;

import com.diario.model.EntradaDiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntradaDiarioRepository extends JpaRepository<EntradaDiario, Long> {
    List<EntradaDiario> findByUsuarioId(Long usuarioId);

    // Busca EntradaDiario e carrega as mídias (EAGERLY)
    @Query("SELECT e FROM EntradaDiario e LEFT JOIN FETCH e.midias WHERE e.id = :id")
    Optional<EntradaDiario> findByIdWithMidias(@Param("id") Long id);
}
