package br.com.financebalance.dto;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PaginaResponse<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {
    public static <E, T> PaginaResponse<T> de(Page<E> page, Function<E, T> mapa) {
        return new PaginaResponse<>(page.getContent().stream().map(mapa).toList(), page.getNumber(),
                page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
