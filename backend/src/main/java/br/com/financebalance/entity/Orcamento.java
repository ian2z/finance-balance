package br.com.financebalance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orcamentos")
@Getter
@Setter
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    /** Sempre o primeiro dia do mes. */
    @Column(nullable = false)
    private LocalDate mes;

    @Column(name = "valor_limite", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorLimite;
}
