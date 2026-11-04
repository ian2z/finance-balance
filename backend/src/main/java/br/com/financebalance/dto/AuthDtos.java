package br.com.financebalance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class AuthDtos {
    private AuthDtos() { }

    public record RegistroRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres") String senha) { }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) { }

    public record UsuarioResponse(Long id, String nome, String email) { }

    public record AuthResponse(String token, String tipo, Instant expiraEm, UsuarioResponse usuario) { }

    public record PerfilRequest(@NotBlank @Size(max = 120) String nome) { }

    public record SenhaRequest(
            @NotBlank String senhaAtual,
            @NotBlank @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres") String novaSenha) { }
}
