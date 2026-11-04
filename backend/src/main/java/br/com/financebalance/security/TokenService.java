package br.com.financebalance.security;

import br.com.financebalance.config.JwtProperties;
import br.com.financebalance.entity.Usuario;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtProperties props;

    public TokenService(JwtEncoder encoder, JwtProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    public record TokenEmitido(String token, Instant expiraEm) { }

    public TokenEmitido emitir(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expira = agora.plus(props.expiration());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("finance-balance")
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getEmail())
                .issuedAt(agora)
                .expiresAt(expira)
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new TokenEmitido(token, expira);
    }
}
