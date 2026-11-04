package br.com.financebalance.repository;

import br.com.financebalance.entity.Etiqueta;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {
    List<Etiqueta> findByUsuarioIdOrderByNomeAsc(Long usuarioId);

    Optional<Etiqueta> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Etiqueta> findByIdInAndUsuarioId(Collection<Long> ids, Long usuarioId);

    boolean existsByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);

    boolean existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(Long usuarioId, String nome, Long id);
}
