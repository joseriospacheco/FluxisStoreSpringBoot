package co.fluxis.store.controllers;

import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.responses.ConsultaProductoRespose;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.dtos.responses.ProductoCreadoResponse;
import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
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
    public ResponseEntity<ProductoCreadoResponse> registrar(
            @Valid @RequestBody CrearProductoRequest dto) {

        Producto producto = productoService.registrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ProductoCreadoResponse(
                        producto.getCodigo(),
                        producto.getNombre(),
                        producto.getPrecio()
                ));
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

        productoService.actualizar(codigo, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> descontinuar(@PathVariable int codigo) {

        productoService.descontinuar(codigo);
        return ResponseEntity.noContent().build();
    }
}
