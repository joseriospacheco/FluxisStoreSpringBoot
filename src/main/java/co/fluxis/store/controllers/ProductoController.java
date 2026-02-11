package co.fluxis.store.controllers;

import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.responses.ProductoRespose;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.ReglaNegocioException;
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
        var productos = productoService.buscarProductos(
                codigo,
                nombre,
                estado,
                precioMin,
                precioMax,
                stockMin,
                stockMax
        );

        if (productos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(productos);
    }

    @PostMapping
    public ResponseEntity<ProductoRespose> registrar(
            @Valid @RequestBody CrearProductoRequest dto) {
        try {
            var productoCreado = productoService.registrarProducto(dto);
            return new ResponseEntity<>(productoCreado, HttpStatus.CREATED);
        } catch (ReglaNegocioException e) {
            return ResponseEntity.unprocessableContent().build();
        } catch (Exception e) {

            System.err.println(e.getMessage());

            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<ProductoRespose> consultar(@PathVariable int codigo) {
        var producto = productoService.consultarPorCodigo(codigo);

        return producto
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> descontinuarProducto(@PathVariable int codigo) {
        boolean descontinuado = productoService.descontinuarProducto(codigo);

        if (descontinuado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<Void> actualizarProducto(
            @PathVariable int codigo,
            @RequestBody ActualizarProductoRequest request
    ) {
        boolean actualizado = productoService.actualizarProducto(codigo, request);

        if (actualizado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }
}