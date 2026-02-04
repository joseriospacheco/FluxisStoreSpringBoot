package co.fluxis.store.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ActualizarProductoRequest(
        @Positive
        Double precio,
        @NotBlank
        @Size(min = 3, max = 100)
        String nombre

) {
}

