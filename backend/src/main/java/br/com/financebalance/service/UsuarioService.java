package br.com.financebalance.service;

import br.com.financebalance.dto.AuthDtos.PerfilRequest;
import br.com.financebalance.dto.AuthDtos.SenhaRequest;
import br.com.financebalance.dto.AuthDtos.UsuarioResponse;
import br.com.financebalance.entity.Usuario;
import br.com.financebalance.exception.NaoEncontradoException;
import br.com.financebalance.exception.RegraNegocioException;
import br.com.financebalance.repository.UsuarioRepository;
import br.com.financebalance.security.UsuarioAtual;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final UsuarioAtual atual;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, UsuarioAtual atual, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.atual = atual;
        this.encoder = encoder;
    }

    private Usuario carregar() {
        return usuarios.findById(atual.id()).orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil() {
        Usuario u = carregar();
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail());
    }

    @Transactional
    public UsuarioResponse atualizar(PerfilRequest req) {
        Usuario u = carregar();
        u.setNome(req.nome().trim());
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail());
    }

    @Transactional
    public void alterarSenha(SenhaRequest req) {
        Usuario u = carregar();
        if (!encoder.matches(req.senhaAtual(), u.getSenhaHash())) {
            throw new RegraNegocioException("Senha atual incorreta", Map.of("senhaAtual", "senha incorreta"));
        }
        u.setSenhaHash(encoder.encode(req.novaSenha()));
    }
}
