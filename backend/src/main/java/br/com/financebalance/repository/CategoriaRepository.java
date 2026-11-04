package br.com.financebalance.repository;

import br.com.financebalance.entity.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /** Categorias padrao (usuario nulo) mais as do proprio usuario. */
    @Query("select c from Categoria c left join fetch c.categoriaPai "
            + "where c.usuario is null or c.usuario.id = :usuarioId order by c.nome")
    List<Categoria> findVisiveis(@Param("usuarioId") Long usuarioId);

    @Query("select c from Categoria c left join fetch c.categoriaPai "
            + "where c.id = :id and (c.usuario is null or c.usuario.id = :usuarioId)")
    Optional<Categoria> findVisivel(@Param("id") Long id, @Param("usuarioId") Long usuarioId);

    boolean existsByCategoriaPaiId(Long paiId);

    boolean existsByUsuarioIdAndNomeIgnoreCaseAndCategoriaPaiIsNullAndIdNot(Long usuarioId, String nome, Long id);

    boolean existsByUsuarioIdAndNomeIgnoreCaseAndCategoriaPaiIdAndIdNot(Long usuarioId, String nome, Long paiId,
                                                                        Long id);
}
