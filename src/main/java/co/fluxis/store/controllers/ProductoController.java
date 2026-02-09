package co.fluxis.store.controllers;

import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.responses.ConsultaProductoRespose;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.EntidadNoEncontradaException;
import co.fluxis.store.exceptions.ReglaNegocioException;
import co.fluxis.store.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<ConsultaProductoRespose> registrar(
            @Valid @RequestBody CrearProductoRequest dto) {


        try {

            Producto producto = productoService.registrar(dto);

            return ResponseEntity.created(URI.create(""))
                    .body(new ConsultaProductoRespose(
                            producto.getCodigo(),
                            producto.getNombre(),
                            producto.getPrecio(),
                            producto.getStock(),
                            producto.getEstado()
                    ));

        } catch (ReglaNegocioException en) {

            return ResponseEntity.badRequest().build();

        } catch (Exception e) {

            return ResponseEntity.internalServerError().build();

        }


    }


    @GetMapping
    public ResponseEntity<List<ConsultaProductoRespose>> buscar(
            @RequestParam(required = false) Integer codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) EstadoProducto estado,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Integer stockMin,
            @RequestParam(required = false) Integer stockMax
    ) {

        List<ConsultaProductoRespose> productos = productoService.buscar(
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
    public ResponseEntity<ConsultaProductoRespose> consultar(@PathVariable int codigo) {

        return productoService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{codigo}")
    public ResponseEntity<Void> actualizar(
            @PathVariable int codigo,
            @RequestBody ActualizarProductoRequest request) {

        try {
            productoService.actualizar(codigo, request);
            return ResponseEntity.noContent().build();

        } catch (ReglaNegocioException en) {
            return ResponseEntity.badRequest().build();
        } catch (Exception enc) {
            return ResponseEntity.badRequest().build();
        }


    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> descontinuar(@PathVariable int codigo) {

        try {

            productoService.descontinuar(codigo);
            return ResponseEntity.noContent().build();

        } catch (ReglaNegocioException en) {
            return ResponseEntity.badRequest().build();
        } catch (EntidadNoEncontradaException enfe) {
            return ResponseEntity.notFound().build();
        }
    }
}

