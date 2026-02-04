package co.fluxis.store.controllers;

import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.responses.ConsultaProductoRespose;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.dtos.responses.ProductoCreadoResponse;
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
    public ResponseEntity<ProductoCreadoResponse> registrar(@Valid @RequestBody CrearProductoRequest dto) {

        if (existeProductoConNombre(dto.nombre())) {
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

        var productoCreado = new ProductoCreadoResponse(nuevoProducto.getCodigo(), nuevoProducto.getNombre(), nuevoProducto.getPrecio());

        return new ResponseEntity<>(productoCreado, HttpStatus.CREATED);

    }


    @GetMapping
    public ResponseEntity<List<ConsultaProductoRespose>> buscarProductos(
            @RequestParam(required = false) Integer codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) EstadoProducto estado,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Integer stockMin,
            @RequestParam(required = false) Integer stockMax
    ) {

        var response = productos.stream()

                .filter(p -> codigo == null || p.getCodigo() == codigo)

                .filter(p -> nombre == null ||
                        p.getNombre().toLowerCase().contains(nombre.toLowerCase()))

                .filter(p -> estado == null || p.getEstado() == estado)

                .filter(p -> precioMin == null || p.getPrecio() >= precioMin)

                .filter(p -> precioMax == null || p.getPrecio() <= precioMax)

                .filter(p -> stockMin == null || p.getStock() >= stockMin)

                .filter(p -> stockMax == null || p.getStock() <= stockMax)

                .map(p -> new ConsultaProductoRespose(
                        p.getCodigo(),
                        p.getNombre(),
                        p.getPrecio(),
                        p.getStock(),
                        p.getEstado()
                ))
                .toList();

        if (response.isEmpty()) {
                return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }



    @GetMapping("/{codigo}")
    public ResponseEntity<ConsultaProductoRespose> consultar(@PathVariable int codigo) {

        var productoOp = productos.stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst();

        if (productoOp.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var response = new ConsultaProductoRespose(
                productoOp.get().getCodigo(),
                productoOp.get().getNombre(),
                productoOp.get().getPrecio(),
                productoOp.get().getStock(),
                productoOp.get().getEstado()
        );

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> descontinuar(@PathVariable int codigo) {

        var productoOp = productos.stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst();

        if (productoOp.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        productoOp.ifPresent(Producto::descontinuar);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<Void> actualizar(
            @PathVariable int codigo,
            @RequestBody ActualizarProductoRequest request
    ) {

        var productoOpt = productos.stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst();

        if (productoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (existeProductoConNombre(request.nombre())) {
            return ResponseEntity.unprocessableContent().build();
        }


        productoOpt.ifPresent(producto -> {

            producto.setNombre(request.nombre());
            producto.setPrecio(request.precio());

        });


        return ResponseEntity.noContent().build();
    }




    private  boolean existeProductoConNombre(String nombre){

        return  productos.stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));

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
