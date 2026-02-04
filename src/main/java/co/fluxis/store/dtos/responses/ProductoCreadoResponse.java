package co.fluxis.store.dtos.responses;

public record ProductoCreadoResponse(
        long codigo,
        String nombre,
        Double precio
) {
}