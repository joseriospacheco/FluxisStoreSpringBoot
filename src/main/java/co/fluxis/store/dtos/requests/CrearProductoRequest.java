package co.fluxis.store.dtos.requests;

import jakarta.validation.constraints.*; // Jakarta EE 9+

public record CrearProductoRequest(
        @NotBlank
        @Size(min = 3, max = 100)
        String nombre,
        @Positive
        Double precio,
        @Positive
        Integer stock
) {}