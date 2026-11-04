package br.com.financebalance.service;

import br.com.financebalance.dto.EtiquetaDtos.EtiquetaRequest;
import br.com.financebalance.dto.RefDto;
import br.com.financebalance.entity.Etiqueta;
import br.com.financebalance.exception.ConflitoException;
import br.com.financebalance.exception.NaoEncontradoException;
import br.com.financebalance.repository.EtiquetaRepository;
import br.com.financebalance.repository.UsuarioRepository;
import br.com.financebalance.security.UsuarioAtual;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EtiquetaService {

    private final EtiquetaRepository etiquetas;
    private final UsuarioRepository usuarios;
    private final UsuarioAtual atual;

    public EtiquetaService(EtiquetaRepository etiquetas, UsuarioRepository usuarios, UsuarioAtual atual) {
        this.etiquetas = etiquetas;
        this.usuarios = usuarios;
        this.atual = atual;
    }

    @Transactional(readOnly = true)
    public List<RefDto> listar() {
        return etiquetas.findByUsuarioIdOrderByNomeAsc(atual.id()).stream().map(this::ref).toList();
    }

    @Transactional
    public RefDto criar(EtiquetaRequest req) {
        Long u = atual.id();
        String nome = req.nome().trim();
        if (etiquetas.existsByUsuarioIdAndNomeIgnoreCase(u, nome)) {
            throw new ConflitoException("Etiqueta já existe");
        }
        Etiqueta e = new Etiqueta();
        e.setUsuario(usuarios.getReferenceById(u));
        e.setNome(nome);
        return ref(etiquetas.save(e));
    }

    @Transactional
    public RefDto atualizar(Long id, EtiquetaRequest req) {
        Long u = atual.id();
        Etiqueta e = obter(id, u);
        String nome = req.nome().trim();
        if (etiquetas.existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(u, nome, id)) {
            throw new ConflitoException("Etiqueta já existe");
        }
        e.setNome(nome);
        return ref(e);
    }

    @Transactional
    public void excluir(Long id) {
        etiquetas.delete(obter(id, atual.id()));
    }

    private Etiqueta obter(Long id, Long u) {
        return etiquetas.findByIdAndUsuarioId(id, u)
                .orElseThrow(() -> new NaoEncontradoException("Etiqueta não encontrada"));
    }

    private RefDto ref(Etiqueta e) {
        return new RefDto(e.getId(), e.getNome());
    }
}
