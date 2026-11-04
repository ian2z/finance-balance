package br.com.financebalance.service;

import br.com.financebalance.dto.AuthDtos.AuthResponse;
import br.com.financebalance.dto.AuthDtos.LoginRequest;
import br.com.financebalance.dto.AuthDtos.RegistroRequest;
import br.com.financebalance.dto.AuthDtos.UsuarioResponse;
import br.com.financebalance.entity.Usuario;
import br.com.financebalance.exception.ConflitoException;
import br.com.financebalance.repository.UsuarioRepository;
import br.com.financebalance.security.TokenService;
import java.util.Locale;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    /** Hash fixo usado para igualar o tempo de resposta quando o e-mail nao existe. */
    private final String hashFalso;

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, TokenService tokens) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.tokens = tokens;
        this.hashFalso = encoder.encode("senha-inexistente");
    }

    static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest req) {
        String email = normalizar(req.email());
        if (usuarios.existsByEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado");
        }
        Usuario u = new Usuario();
        u.setNome(req.nome().trim());
        u.setEmail(email);
        u.setSenhaHash(encoder.encode(req.senha()));
        return resposta(usuarios.save(u));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarios.findByEmail(normalizar(req.email())).orElse(null);
        boolean ok = encoder.matches(req.senha(), u != null ? u.getSenhaHash() : hashFalso);
        if (u == null || !ok) {
            throw new BadCredentialsException("credenciais inválidas");
        }
        return resposta(u);
    }

    private AuthResponse resposta(Usuario u) {
        var token = tokens.emitir(u);
        return new AuthResponse(token.token(), "Bearer", token.expiraEm(),
                new UsuarioResponse(u.getId(), u.getNome(), u.getEmail()));
    }
}
