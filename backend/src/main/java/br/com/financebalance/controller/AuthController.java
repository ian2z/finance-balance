package br.com.financebalance.controller;

import br.com.financebalance.dto.AuthDtos.AuthResponse;
import br.com.financebalance.dto.AuthDtos.LoginRequest;
import br.com.financebalance.dto.AuthDtos.RegistroRequest;
import br.com.financebalance.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação")
@SecurityRequirements
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um usuário e retorna o JWT")
    public AuthResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return service.registrar(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica e retorna o JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }
}
