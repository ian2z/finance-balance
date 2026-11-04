package br.com.financebalance.dev;

import br.com.financebalance.entity.*;
import br.com.financebalance.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dados de demonstração para desenvolvimento. Só roda com o profile "dev" e é idempotente:
 * usuários que já existem (por e-mail) são ignorados.
 */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    public static final String SENHA_DEMO = "Demo@1234";
    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UsuarioRepository usuarios;
    private final ContaRepository contas;
    private final CategoriaRepository categorias;
    private final FormaPagamentoRepository formas;
    private final EtiquetaRepository etiquetas;
    private final TransacaoRepository transacoes;
    private final OrcamentoRepository orcamentos;
    private final RegraDistribuicaoRepository regras;
    private final InvestimentoRepository investimentos;
    private final MovimentoInvestimentoRepository movimentos;
    private final PasswordEncoder encoder;

    public DevDataSeeder(UsuarioRepository usuarios, ContaRepository contas, CategoriaRepository categorias,
                         FormaPagamentoRepository formas, EtiquetaRepository etiquetas,
                         TransacaoRepository transacoes, OrcamentoRepository orcamentos,
                         RegraDistribuicaoRepository regras, InvestimentoRepository investimentos,
                         MovimentoInvestimentoRepository movimentos, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.contas = contas;
        this.categorias = categorias;
        this.formas = formas;
        this.etiquetas = etiquetas;
        this.transacoes = transacoes;
        this.orcamentos = orcamentos;
        this.regras = regras;
        this.investimentos = investimentos;
        this.movimentos = movimentos;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        semear("Ana Souza", "ana@demo.com", new BigDecimal("4500.00"), 1, "Pet", "Ração");
        semear("Bruno Lima", "bruno@demo.com", new BigDecimal("3200.00"), 2, "Games", "Assinaturas");
        semearVazio("Carla Nova", "novo@demo.com");
    }

    /** Usuário sem nenhum dado, para testar telas vazias e o primeiro uso. */
    private void semearVazio(String nome, String email) {
        if (!usuarios.existsByEmail(email)) {
            criarUsuario(nome, email, null);
            log.info("Seed dev: usuário vazio {} criado", email);
        }
    }

    private Usuario criarUsuario(String nome, String email, BigDecimal renda) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenhaHash(encoder.encode(SENHA_DEMO));
        u.setRendaMensal(renda);
        return usuarios.save(u);
    }

    private void semear(String nome, String email, BigDecimal renda, long semente, String catPersonalizada,
                        String subPersonalizada) {
        if (usuarios.existsByEmail(email)) {
            return;
        }
        Usuario u = criarUsuario(nome, email, renda);
        Random rnd = new Random(semente);
        LocalDate hoje = LocalDate.now();

        Conta corrente = conta(u, "Conta Corrente", TipoConta.CORRENTE, "2500.00");
        Conta reserva = conta(u, "Reserva", TipoConta.POUPANCA, "5000.00");
        Conta carteira = conta(u, "Carteira", TipoConta.DINHEIRO, "150.00");

        Map<String, Categoria> cat = categorias.findVisiveis(u.getId()).stream()
                .collect(Collectors.toMap(Categoria::getNome, Function.identity(), (a, b) -> a));
        Categoria personalizada = categoria(u, catPersonalizada, TipoCategoria.DESPESA, null);
        Categoria sub = categoria(u, subPersonalizada, TipoCategoria.DESPESA, personalizada);

        Etiqueta viagem = etiqueta(u, "Viagem");
        Etiqueta essencial = etiqueta(u, "Essencial");
        Map<String, FormaPagamento> forma = formas.findAll().stream()
                .collect(Collectors.toMap(FormaPagamento::getNome, Function.identity()));

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(hoje).minusMonths(i);
            boolean atual = i == 0;
            int ultimoDia = atual ? hoje.getDayOfMonth() : 28;

            // Fixas
            tx(u, TipoTransacao.RECEITA, renda, ym, 1, ultimoDia, cat.get("Salário"), corrente, forma.get("Transferência bancária"), "Salário", Set.of());
            tx(u, TipoTransacao.DESPESA, new BigDecimal("1200.00"), ym, 5, ultimoDia, cat.get("Moradia"), corrente, forma.get("Boleto"), "Aluguel", Set.of(essencial));
            tx(u, TipoTransacao.DESPESA, new BigDecimal("89.90"), ym, 10, ultimoDia, cat.get("Moradia"), corrente, forma.get("Boleto"), "Internet", Set.of(essencial));
            // Variáveis (valores determinísticos por semente)
            for (int k = 0; k < 3; k++) {
                tx(u, TipoTransacao.DESPESA, valor(rnd, 120, 380), ym, 3 + k * 9, ultimoDia, cat.get("Alimentação"), corrente, forma.get("Cartão de débito"), "Mercado", Set.of(essencial));
            }
            tx(u, TipoTransacao.DESPESA, valor(rnd, 60, 160), ym, 12, ultimoDia, cat.get("Transporte"), corrente, forma.get("Pix"), "Combustível", Set.of());
            tx(u, TipoTransacao.DESPESA, valor(rnd, 40, 110), ym, 20, ultimoDia, cat.get("Transporte"), carteira, forma.get("Dinheiro"), "Ônibus e apps", Set.of());
            tx(u, TipoTransacao.DESPESA, valor(rnd, 50, 180), ym, 15, ultimoDia, cat.get("Lazer"), corrente, forma.get("Cartão de crédito"), "Cinema e jantar", Set.of());
            tx(u, TipoTransacao.DESPESA, valor(rnd, 40, 140), ym, 18, ultimoDia, sub, corrente, forma.get("Pix"), subPersonalizada, Set.of());
            if (i % 2 == 0) {
                tx(u, TipoTransacao.RECEITA, valor(rnd, 200, 700), ym, 22, ultimoDia, cat.get("Freelance"), corrente, forma.get("Pix"), "Projeto freelance", Set.of());
            }
            if (i == 3) {
                tx(u, TipoTransacao.DESPESA, new BigDecimal("950.00"), ym, 14, ultimoDia, cat.get("Lazer"), corrente, forma.get("Cartão de crédito"), "Passagens de viagem", Set.of(viagem));
            }
            // Transferência mensal para a reserva
            Transacao t = base(u, TipoTransacao.TRANSFERENCIA, new BigDecimal("400.00"), data(ym, 6, ultimoDia), "Reserva mensal");
            t.setConta(corrente);
            t.setContaDestino(reserva);
            transacoes.save(t);
        }

        // Pendentes do mês atual (a pagar nos próximos dias)
        YearMonth agora = YearMonth.from(hoje);
        pendente(u, new BigDecimal("320.00"), agora.atDay(Math.min(28, hoje.getDayOfMonth() + 5)), cat.get("Educação"), corrente, "Mensalidade do curso");
        pendente(u, new BigDecimal("180.00"), agora.atDay(Math.min(28, hoje.getDayOfMonth() + 8)), cat.get("Saúde"), corrente, "Plano de saúde");

        // Orçamentos do mês
        orcamento(u, cat.get("Alimentação"), agora, "800.00");
        orcamento(u, cat.get("Lazer"), agora, "200.00");
        orcamento(u, cat.get("Transporte"), agora, "300.00");

        // Distribuição planejada da renda
        distribuicao(u, cat.get("Moradia"), "30");
        distribuicao(u, cat.get("Alimentação"), "25");
        distribuicao(u, cat.get("Transporte"), "8");
        distribuicao(u, cat.get("Lazer"), "7");
        distribuicao(u, cat.get("Educação"), "10");

        // Investimentos
        Investimento cdb = investimento(u, "CDB 110% CDI", TipoInvestimento.CDB, hoje.minusMonths(8), "2000.00");
        mov(cdb, TipoMovimento.APORTE, "500.00", hoje.minusMonths(4));
        mov(cdb, TipoMovimento.RENDIMENTO, "163.40", hoje.minusDays(10));
        Investimento tesouro = investimento(u, "Tesouro Selic 2029", TipoInvestimento.TESOURO_DIRETO, hoje.minusMonths(5), "1500.00");
        mov(tesouro, TipoMovimento.RENDIMENTO, "92.10", hoje.minusDays(5));
        Investimento fii = investimento(u, "FII HGLG11", TipoInvestimento.FII, hoje.minusMonths(3), "1000.00");
        mov(fii, TipoMovimento.RESGATE, "300.00", hoje.minusDays(20));

        log.info("Seed dev: usuário {} criado com dados de demonstração", email);
    }

    // ---------- helpers ----------

    private Conta conta(Usuario u, String nome, TipoConta tipo, String saldo) {
        Conta c = new Conta();
        c.setUsuario(u);
        c.setNome(nome);
        c.setTipo(tipo);
        c.setSaldoInicial(new BigDecimal(saldo));
        return contas.save(c);
    }

    private Categoria categoria(Usuario u, String nome, TipoCategoria tipo, Categoria pai) {
        Categoria c = new Categoria();
        c.setUsuario(u);
        c.setNome(nome);
        c.setTipo(tipo);
        c.setCategoriaPai(pai);
        return categorias.save(c);
    }

    private Etiqueta etiqueta(Usuario u, String nome) {
        Etiqueta e = new Etiqueta();
        e.setUsuario(u);
        e.setNome(nome);
        return etiquetas.save(e);
    }

    private static BigDecimal valor(Random rnd, int min, int max) {
        return BigDecimal.valueOf(min + rnd.nextInt(max - min) + rnd.nextInt(100) / 100.0)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static LocalDate data(YearMonth ym, int dia, int maximo) {
        return ym.atDay(Math.max(1, Math.min(dia, maximo)));
    }

    private Transacao base(Usuario u, TipoTransacao tipo, BigDecimal valor, LocalDate data, String descricao) {
        Transacao t = new Transacao();
        t.setUsuario(u);
        t.setTipo(tipo);
        t.setValor(valor);
        t.setData(data);
        t.setDescricao(descricao);
        t.setSituacao(SituacaoTransacao.PAGA);
        return t;
    }

    private void tx(Usuario u, TipoTransacao tipo, BigDecimal valor, YearMonth ym, int dia, int maximo,
                    Categoria cat, Conta conta, FormaPagamento forma, String descricao, Set<Etiqueta> tags) {
        Transacao t = base(u, tipo, valor, data(ym, dia, maximo), descricao);
        t.setCategoria(cat);
        t.setConta(conta);
        t.setFormaPagamento(forma);
        t.setEtiquetas(new LinkedHashSet<>(tags));
        transacoes.save(t);
    }

    private void pendente(Usuario u, BigDecimal valor, LocalDate data, Categoria cat, Conta conta, String descricao) {
        Transacao t = base(u, TipoTransacao.DESPESA, valor, data, descricao);
        t.setSituacao(SituacaoTransacao.PENDENTE);
        t.setDataVencimento(data);
        t.setCategoria(cat);
        t.setConta(conta);
        transacoes.save(t);
    }

    private void orcamento(Usuario u, Categoria cat, YearMonth mes, String limite) {
        Orcamento o = new Orcamento();
        o.setUsuario(u);
        o.setCategoria(cat);
        o.setMes(mes.atDay(1));
        o.setValorLimite(new BigDecimal(limite));
        orcamentos.save(o);
    }

    private void distribuicao(Usuario u, Categoria cat, String percentual) {
        RegraDistribuicao r = new RegraDistribuicao();
        r.setUsuario(u);
        r.setCategoria(cat);
        r.setPercentual(new BigDecimal(percentual));
        regras.save(r);
    }

    private Investimento investimento(Usuario u, String nome, TipoInvestimento tipo, LocalDate data, String aportado) {
        Investimento i = new Investimento();
        i.setUsuario(u);
        i.setNome(nome);
        i.setTipo(tipo);
        i.setDataAplicacao(data);
        i.setValorAplicado(new BigDecimal(aportado));
        i.setValorAtual(new BigDecimal(aportado));
        investimentos.save(i);
        registrar(i, TipoMovimento.APORTE, new BigDecimal(aportado), data);
        return i;
    }

    /** Aplica o efeito do movimento no investimento (mesmas regras do InvestimentoService) e o registra. */
    private void mov(Investimento i, TipoMovimento tipo, String valor, LocalDate data) {
        BigDecimal v = new BigDecimal(valor);
        if (v.signum() <= 0) {
            return;
        }
        switch (tipo) {
            case APORTE -> {
                i.setValorAplicado(i.getValorAplicado().add(v));
                i.setValorAtual(i.getValorAtual().add(v));
            }
            case RESGATE -> {
                BigDecimal custo = i.getValorAplicado().multiply(v)
                        .divide(i.getValorAtual(), 2, java.math.RoundingMode.HALF_UP);
                i.setValorAplicado(i.getValorAplicado().subtract(custo));
                i.setValorAtual(i.getValorAtual().subtract(v));
            }
            case RENDIMENTO -> i.setValorAtual(i.getValorAtual().add(v));
        }
        registrar(i, tipo, v, data);
    }

    private void registrar(Investimento i, TipoMovimento tipo, BigDecimal valor, LocalDate data) {
        MovimentoInvestimento m = new MovimentoInvestimento();
        m.setInvestimento(i);
        m.setTipo(tipo);
        m.setValor(valor);
        m.setData(data);
        movimentos.save(m);
    }
}
