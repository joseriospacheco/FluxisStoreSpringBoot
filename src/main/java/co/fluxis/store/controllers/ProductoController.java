package co.fluxis.store.controllers;

import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.responses.ProductoRespose;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.EntidadNoEncontradaException;
import co.fluxis.store.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<ProductoRespose> registrar(
            @Valid @RequestBody CrearProductoRequest dto) {

        ProductoRespose producto = productoService.registrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(producto);
    }


    @GetMapping
    public ResponseEntity<List<ProductoRespose>> buscarProductos(
            @RequestParam(required = false) Integer codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) EstadoProducto estado,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Integer stockMin,
            @RequestParam(required = false) Integer stockMax
    ) {

        List<ProductoRespose> productos = productoService.buscar(
                codigo,
                nombre,
                estado,
                precioMin,
                precioMax,
                stockMin,
                stockMax
        );

        if (productos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(productos);
    }


    @GetMapping("/{codigo}")
    public ResponseEntity<ProductoRespose> consultar(@PathVariable int codigo) {

        return productoService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{codigo}")
    public ResponseEntity<Void> actualizar(
            @PathVariable int codigo,
            @RequestBody ActualizarProductoRequest request) {

        try {

            if (productoService.actualizar(codigo, request)) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.badRequest().build();

        } catch (EntidadNoEncontradaException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }


    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> descontinuar(@PathVariable int codigo) {

        try {

            if (productoService.descontinuar(codigo)) {
                return ResponseEntity.noContent().build();
            }

        } catch (EntidadNoEncontradaException ene) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.badRequest().build();
    }
}
