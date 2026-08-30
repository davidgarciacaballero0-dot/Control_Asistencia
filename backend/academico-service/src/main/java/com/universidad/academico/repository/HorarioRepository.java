package com.universidad.academico.repository;

import com.universidad.academico.domain.DiaSemana;
import com.universidad.academico.domain.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Horario.
 */
@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByGrupoId(Long grupoId);

    List<Horario> findByDia(DiaSemana dia);
}
