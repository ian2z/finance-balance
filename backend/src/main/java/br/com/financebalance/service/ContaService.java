package br.com.financebalance.service;

import br.com.financebalance.dto.ContaDtos.ContaRequest;
import br.com.financebalance.dto.ContaDtos.ContaResponse;
import br.com.financebalance.entity.Conta;
import br.com.financebalance.entity.SituacaoTransacao;
import br.com.financebalance.entity.TipoTransacao;
import br.com.financebalance.exception.ConflitoException;
import br.com.financebalance.exception.NaoEncontradoException;
import br.com.financebalance.repository.ContaRepository;
import br.com.financebalance.repository.TransacaoRepository;
import br.com.financebalance.repository.UsuarioRepository;
import br.com.financebalance.security.UsuarioAtual;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

    private final ContaRepository contas;
    private final TransacaoRepository transacoes;
    private final UsuarioRepository usuarios;
    private final UsuarioAtual atual;

    public ContaService(ContaRepository contas, TransacaoRepository transacoes, UsuarioRepository usuarios,
                        UsuarioAtual atual) {
        this.contas = contas;
        this.transacoes = transacoes;
        this.usuarios = usuarios;
        this.atual = atual;
    }

    /**
     * Saldo atual por conta: saldo inicial + receitas pagas - despesas pagas
     * - transferencias pagas enviadas + transferencias pagas recebidas.
     */
    public Map<Long, BigDecimal> saldosAtuais(Long usuarioId, List<Conta> lista) {
        Map<Long, BigDecimal> saldos = new HashMap<>();
        lista.forEach(c -> saldos.put(c.getId(), c.getSaldoInicial()));
        for (Object[] l : transacoes.movimentosPorConta(usuarioId, SituacaoTransacao.PAGA)) {
            Long contaId = (Long) l[0];
            BigDecimal soma = (BigDecimal) l[2];
            TipoTransacao tipo = (TipoTransacao) l[1];
            BigDecimal delta = tipo == TipoTransacao.RECEITA ? soma : soma.negate();
            saldos.computeIfPresent(contaId, (k, v) -> v.add(delta));
        }
        for (Object[] l : transacoes.entradasPorConta(usuarioId, SituacaoTransacao.PAGA, TipoTransacao.TRANSFERENCIA)) {
            saldos.computeIfPresent((Long) l[0], (k, v) -> v.add((BigDecimal) l[1]));
        }
        return saldos;
    }

    @Transactional(readOnly = true)
    public List<ContaResponse> listar() {
        Long u = atual.id();
        List<Conta> lista = contas.findByUsuarioIdOrderByNomeAsc(u);
        Map<Long, BigDecimal> saldos = saldosAtuais(u, lista);
        return lista.stream().map(c -> resposta(c, saldos.get(c.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public ContaResponse buscar(Long id) {
        Long u = atual.id();
        Conta c = obter(id, u);
        return resposta(c, saldosAtuais(u, List.of(c)).get(c.getId()));
    }

    @Transactional
    public ContaResponse criar(ContaRequest req) {
        Long u = atual.id();
        String nome = req.nome().trim();
        if (contas.existsByUsuarioIdAndNomeIgnoreCase(u, nome)) {
            throw new ConflitoException("Já existe uma conta com esse nome");
        }
        Conta c = new Conta();
        c.setUsuario(usuarios.getReferenceById(u));
        aplicar(c, req, nome);
        contas.save(c);
        return buscar(c.getId());
    }

    @Transactional
    public ContaResponse atualizar(Long id, ContaRequest req) {
        Long u = atual.id();
        Conta c = obter(id, u);
        String nome = req.nome().trim();
        if (contas.existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(u, nome, id)) {
            throw new ConflitoException("Já existe uma conta com esse nome");
        }
        aplicar(c, req, nome);
        return buscar(id);
    }

    @Transactional
    public void excluir(Long id) {
        Conta c = obter(id, atual.id());
        if (transacoes.existsByContaIdOrContaDestinoId(id, id)) {
            throw new ConflitoException("A conta possui movimentações; desative-a em vez de excluir");
        }
        contas.delete(c);
    }

    private void aplicar(Conta c, ContaRequest req, String nome) {
        c.setNome(nome);
        c.setTipo(req.tipo());
        c.setSaldoInicial(req.saldoInicial());
        if (req.ativa() != null) {
            c.setAtiva(req.ativa());
        }
    }

    private Conta obter(Long id, Long usuarioId) {
        return contas.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new NaoEncontradoException("Conta não encontrada"));
    }

    private ContaResponse resposta(Conta c, BigDecimal saldoAtual) {
        return new ContaResponse(c.getId(), c.getNome(), c.getTipo(), c.getSaldoInicial(), saldoAtual, c.isAtiva());
    }
}
