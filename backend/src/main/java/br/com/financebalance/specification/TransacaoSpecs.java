package br.com.financebalance.specification;

import br.com.financebalance.entity.Transacao;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class TransacaoSpecs {

    private TransacaoSpecs() { }

    public static Specification<Transacao> filtrar(Long usuarioId, TransacaoFiltro f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("usuario").get("id"), usuarioId));

            if (f.texto() != null && !f.texto().isBlank()) {
                String termo = f.texto().trim().toLowerCase()
                        .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                p.add(cb.like(cb.lower(root.get("descricao")), "%" + termo + "%", '\\'));
            }
            if (f.categoriaId() != null) {
                Join<?, ?> cat = root.join("categoria", JoinType.LEFT);
                Join<?, ?> pai = cat.join("categoriaPai", JoinType.LEFT);
                p.add(cb.or(cb.equal(cat.get("id"), f.categoriaId()), cb.equal(pai.get("id"), f.categoriaId())));
            }
            if (f.contaId() != null) {
                p.add(cb.or(cb.equal(root.get("conta").get("id"), f.contaId()),
                        cb.equal(root.get("contaDestino").get("id"), f.contaId())));
            }
            if (f.tipo() != null) {
                p.add(cb.equal(root.get("tipo"), f.tipo()));
            }
            if (f.situacao() != null) {
                p.add(cb.equal(root.get("situacao"), f.situacao()));
            }
            if (f.etiquetaId() != null) {
                p.add(cb.equal(root.join("etiquetas", JoinType.INNER).get("id"), f.etiquetaId()));
            }
            if (f.de() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("data"), f.de()));
            }
            if (f.ate() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("data"), f.ate()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
