package co.fluxis.store.dtos.responses;

import co.fluxis.store.enums.EstadoProducto;

public record ConsultaProductoResposeDTO(
        long codigo,
        String nombre,
        Double precio,
        int stock,
        EstadoProducto estado

) {
}
