package co.fluxis.store.controllers;

import co.fluxis.store.dtos.requests.ActualizarProductoRequestDTO;
import co.fluxis.store.dtos.responses.ConsultaProductoResposeDTO;
import co.fluxis.store.dtos.requests.CrearProductoRequestDTO;
import co.fluxis.store.dtos.responses.ProductoCreadoResponseDTO;
import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.ReglaNegocioException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/productos")
public class ProductoController {

    private static List<Producto> productos;

    public ProductoController() {
        cargarProductosIniciales();
    }

    @PostMapping
    public ResponseEntity<ProductoCreadoResponseDTO> registrar(@Valid @RequestBody CrearProductoRequestDTO dto) {

        // Validar nombre duplicado
        var nombreExiste = productos.stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(dto.nombre()));

        if (nombreExiste) {
            //throw new ReglaNegocioException("Ya existe un producto con el nombre: " + dto.nombre());
            return ResponseEntity.unprocessableContent().build();
        }

        Producto nuevoProducto = null;

        try {
            nuevoProducto = new Producto(dto.nombre(), dto.precio(), dto.stock());

        } catch (ReglaNegocioException e) {

            return ResponseEntity.unprocessableContent().build();

        } catch (Exception e) {

            return ResponseEntity.badRequest().build();
        }

        productos.add(nuevoProducto);

        var productoCreado = new ProductoCreadoResponseDTO(nuevoProducto.getCodigo(), nuevoProducto.getNombre(), nuevoProducto.getPrecio());

        return new ResponseEntity<>(productoCreado, HttpStatus.CREATED);

    }


    @GetMapping
    public ResponseEntity<List<ConsultaProductoResposeDTO>> consultar() {

        var response = productos.stream()
                .filter(p -> p.getEstado() == EstadoProducto.DISPONIBLE)
                .map(p -> new ConsultaProductoResposeDTO(
                        p.getCodigo(),
                        p.getNombre(),
                        p.getPrecio(),
                        p.getStock(),
                        p.getEstado()
                )).toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{codigo}")
    public ResponseEntity<ConsultaProductoResposeDTO> consultar(@PathVariable int codigo) {

        var producto = productos.stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst();

        if (producto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var response = new ConsultaProductoResposeDTO(
                producto.get().getCodigo(),
                producto.get().getNombre(),
                producto.get().getPrecio(),
                producto.get().getStock(),
                producto.get().getEstado()
        );

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> descontinuarProducto(@PathVariable int codigo) {

        var producto = productos.stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst();

        if (producto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        producto.get().setEstado(EstadoProducto.DESCONTINUADO);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<Void> actualizarProducto(
            @PathVariable long codigo,
            @RequestBody ActualizarProductoRequestDTO request
    ) {

        var producto = productos.stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst();

        if (producto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        producto.ifPresent(p -> {

            try {

                p.setPrecio(request.precio());
                p.agregarAlStock(request.stock() - p.getStock());

            } catch (ReglaNegocioException e) {
                throw new RuntimeException(e);
            }

        });


            /*
            producto.get().setPrecio(request.precio());
            producto.get().agregarAlStock(request.stock() - producto.get().getStock());
             */


        return ResponseEntity.noContent().build();
    }

    private static void cargarProductosIniciales() {
        try {

            productos = new ArrayList<>();

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
