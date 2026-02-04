package co.fluxis.store.services;

import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.dtos.responses.ConsultaProductoRespose;
import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.ReglaNegocioException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final List<Producto> productos = new ArrayList<>();

    public Producto registrar(CrearProductoRequest dto) {

        boolean nombreExiste = productos.stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(dto.nombre()));

        if (nombreExiste) {
            throw new ReglaNegocioException(
                    "Ya existe un producto con el nombre: " + dto.nombre()
            );
        }

        Producto producto = new Producto(
                dto.nombre(),
                dto.precio(),
                dto.stock()
        );

        productos.add(producto);
        return producto;
    }

    public List<ConsultaProductoRespose> buscar(
            Integer codigo,
            String nombre,
            EstadoProducto estado,
            Double precioMin,
            Double precioMax,
            Integer stockMin,
            Integer stockMax
    ) {

        return productos.stream()
                .filter(p -> codigo == null || p.getCodigo() == codigo)
                .filter(p -> nombre == null ||
                        p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .filter(p -> estado == null || p.getEstado() == estado)
                .filter(p -> precioMin == null || p.getPrecio() >= precioMin)
                .filter(p -> precioMax == null || p.getPrecio() <= precioMax)
                .filter(p -> stockMin == null || p.getStock() >= stockMin)
                .filter(p -> stockMax == null || p.getStock() <= stockMax)

                // 🔽 MAPE0 A DTO
                .map(p -> new ConsultaProductoRespose(
                        p.getCodigo(),
                        p.getNombre(),
                        p.getPrecio(),
                        p.getStock(),
                        p.getEstado()
                ))
                .toList();
    }


    public Optional<ConsultaProductoRespose> buscarPorCodigo(int codigo) {
        return productos.stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst()
                .map(p -> new ConsultaProductoRespose(
                        p.getCodigo(),
                        p.getNombre(),
                        p.getPrecio(),
                        p.getStock(),
                        p.getEstado()
                ));
    }


    private Optional<Producto> buscar(int codigo) {
        return productos.stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst();
    }


    public void descontinuar(int codigo) {
        var producto = buscar(codigo)
                .orElseThrow(() -> new ReglaNegocioException("Producto no encontrado"));
        producto.descontinuar();
    }

    public void actualizar(int codigo, ActualizarProductoRequest request) {
        var producto = buscar(codigo)
                .orElseThrow();

        producto.setNombre(request.nombre());
        producto.setPrecio(request.precio());
    }
}
