package br.com.financebalance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movimentos_investimento")
@Getter
@Setter
public class MovimentoInvestimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investimento_id")
    private Investimento investimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoMovimento tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;
}
