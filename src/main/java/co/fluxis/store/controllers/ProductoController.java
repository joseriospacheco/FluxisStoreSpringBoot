package co.fluxis.store.controllers;

import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.responses.ProductoRespose;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.EntityNotFoundException;
import co.fluxis.store.exceptions.ReglaNegocioException;
import co.fluxis.store.services.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }



    @GetMapping
    public ResponseEntity<List<ProductoRespose>> Listar(){

        return ResponseEntity.ok(productoService.listar());
    }


    /*

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

*/


    @PostMapping
    public ResponseEntity<ProductoRespose> registrar(@RequestBody CrearProductoRequest dto) {

        try {

            ProductoRespose productoCreado = productoService.registrarProducto(dto);
            return ResponseEntity.created(URI.create("api/producto/"+productoCreado.codigo()))
                                 .body(productoCreado);

        } catch (ReglaNegocioException e) {

            return ResponseEntity.unprocessableContent().build();

        } catch (Exception e) {

            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<ProductoRespose> consultar(@PathVariable int codigo) {
        Optional<ProductoRespose>producto = productoService.consultarPorCodigo(codigo);

        return producto
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    /*

    @GetMapping("/{codigo}")
    public ResponseEntity<ProductoRespose> consultar2(@PathVariable int codigo) {



        var producto = productoService.consultarPorCodigo(codigo);

        return producto
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

*/

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
        try {
            if (productoService.actualizar(codigo, request))
                return ResponseEntity.noContent().build();
            else
                return  ResponseEntity.badRequest().build();

        }catch (EntityNotFoundException e){
            return  ResponseEntity.notFound().build();
        }catch (ReglaNegocioException e){
            return  ResponseEntity.unprocessableContent().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }
}