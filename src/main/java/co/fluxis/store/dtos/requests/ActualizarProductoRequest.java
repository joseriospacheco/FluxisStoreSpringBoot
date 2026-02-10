package co.fluxis.store.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ActualizarProductoRequest(

        @NotBlank
        @Size(min = 3, max = 100)
        String nombre,
        @Positive
        Double precio

) {
}

