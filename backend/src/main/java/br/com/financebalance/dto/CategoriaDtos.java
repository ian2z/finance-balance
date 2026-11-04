package br.com.financebalance.dto;

import br.com.financebalance.entity.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CategoriaDtos {
    private CategoriaDtos() { }

    public record CategoriaRequest(
            @NotBlank @Size(max = 80) String nome,
            @NotNull TipoCategoria tipo,
            Long categoriaPaiId) { }

    public record CategoriaResponse(Long id, String nome, TipoCategoria tipo, Long categoriaPaiId,
                                    boolean doSistema, boolean semMovimentacoes) { }
}
