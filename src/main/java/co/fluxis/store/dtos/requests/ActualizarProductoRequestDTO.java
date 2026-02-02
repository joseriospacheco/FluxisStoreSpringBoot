package co.fluxis.store.dtos.requests;

import co.fluxis.store.enums.EstadoProducto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ActualizarProductoRequestDTO(
        @Positive
        Double precio,
        @NotNull
        EstadoProducto estado

) {
}

