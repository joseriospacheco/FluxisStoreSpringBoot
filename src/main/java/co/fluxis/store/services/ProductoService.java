package co.fluxis.store.services;

import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.dtos.responses.ProductoRespose;
import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.EntidadNoEncontradaException;
import co.fluxis.store.exceptions.ReglaNegocioException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final static List<Producto> productos = new ArrayList<>();

    public ProductoService() {

        cargarProductosIniciales();

    }


    private  boolean existeProductoConNombre(String nombre){

        return  productos.stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));

    }

    public Producto registrar(CrearProductoRequest dto) {

        if (existeProductoConNombre(dto.nombre())) {
            throw new ReglaNegocioException("Ya existe un producto con el nombre: " + dto.nombre());
        }

        Producto producto = new Producto(
                dto.nombre(),
                dto.precio(),
                dto.stock()
        );

        productos.add(producto);
        return producto;
    }

    public List<ProductoRespose> buscar(
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
                .filter(p -> nombre == null || p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .filter(p -> estado == null || p.getEstado() == estado)
                .filter(p -> precioMin == null || p.getPrecio() >= precioMin)
                .filter(p -> precioMax == null || p.getPrecio() <= precioMax)
                .filter(p -> stockMin == null || p.getStock() >= stockMin)
                .filter(p -> stockMax == null || p.getStock() <= stockMax)
                .map(p -> new ProductoRespose(
                        p.getCodigo(),
                        p.getNombre(),
                        p.getPrecio(),
                        p.getStock(),
                        p.getEstado()
                ))
                .toList();
    }


    public Optional<ProductoRespose> buscarPorCodigo(int codigo) {
        return productos.stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst()
                .map(p -> new ProductoRespose(
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
                .orElseThrow(() -> new EntidadNoEncontradaException("Producto no encontrado"));
        producto.descontinuar();
    }

    public void actualizar(int codigo, ActualizarProductoRequest dto) {
        var producto = buscar(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("Producto no encontrado"));

        if (existeProductoConNombre(dto.nombre())) {
            throw new ReglaNegocioException("Ya existe un producto con el nombre: " + dto.nombre());
        }

        producto.setNombre(dto.nombre());
        producto.setPrecio(dto.precio());
    }


    private void cargarProductosIniciales() {
        try {


            productos.add(new Producto("Laptop Lenovo IdeaPad 3", 2450000, 5));
            productos.add(new Producto("Mouse Logitech Inalambrico", 85000, 30));
            productos.add(new Producto("Teclado Mecanico Redragon", 195000, 12));
            productos.add(new Producto("Monitor Samsung 24 Pulgadas", 920000, 7));
            productos.add(new Producto("Disco Solido SSD Kingston 480GB", 210000, 18));
            productos.add(new Producto("Memoria RAM DDR4 16GB Corsair", 265000, 10));
            productos.add(new Producto("Audifonos Gamer HyperX", 175000, 20));
            productos.add(new Producto("Impresora Epson EcoTank L3250", 1350000, 4));
            productos.add(new Producto("Router TP Link Archer C6", 185000, 9));
            productos.add(new Producto("Webcam Logitech HD 1080p", 320000, 6));

        } catch (ReglaNegocioException e) {
            System.out.println("Error cargando productos de prueba: " + e.getMessage());
        }
    }
}
