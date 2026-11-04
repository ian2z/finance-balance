package br.com.financebalance.controller;

import br.com.financebalance.dto.AuthDtos.PerfilRequest;
import br.com.financebalance.dto.AuthDtos.SenhaRequest;
import br.com.financebalance.dto.AuthDtos.UsuarioResponse;
import br.com.financebalance.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios/me")
@Tag(name = "Usuário")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public UsuarioResponse perfil() {
        return service.perfil();
    }

    @PutMapping
    public UsuarioResponse atualizar(@Valid @RequestBody PerfilRequest req) {
        return service.atualizar(req);
    }

    @PutMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@Valid @RequestBody SenhaRequest req) {
        service.alterarSenha(req);
    }
}
