package co.fluxis.store.dtos.requests;

import jakarta.validation.constraints.Positive;

public record ActualizarProductoRequestDTO(
        @Positive
        Double precio,
        @Positive
        Integer stock
) {
}

