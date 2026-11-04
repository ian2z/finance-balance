package br.com.financebalance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class EtiquetaDtos {
    private EtiquetaDtos() { }

    public record EtiquetaRequest(@NotBlank @Size(max = 40) String nome) { }
}
