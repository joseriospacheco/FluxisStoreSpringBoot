package co.fluxis.store.dtos.responses;

public record ProductoCreadoResponseDTO(
        long codigo,
        String nombre,
        Double precio
) {
}