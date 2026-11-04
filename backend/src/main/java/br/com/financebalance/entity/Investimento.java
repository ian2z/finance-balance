package br.com.financebalance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "investimentos")
@Getter
@Setter
public class Investimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoInvestimento tipo;

    @Column(name = "valor_aplicado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAplicado = BigDecimal.ZERO;

    @Column(name = "valor_atual", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAtual = BigDecimal.ZERO;

    @Column(name = "data_aplicacao", nullable = false)
    private LocalDate dataAplicacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();
}
