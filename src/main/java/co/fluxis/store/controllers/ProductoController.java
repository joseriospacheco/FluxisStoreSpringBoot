package co.fluxis.store.controllers;

import co.fluxis.store.dtos.requests.ActualizarProductoRequestDTO;
import co.fluxis.store.dtos.responses.ConsultaProductoResposeDTO;
import co.fluxis.store.dtos.requests.CrearProductoRequestDTO;
import co.fluxis.store.dtos.responses.ProductoCreadoResponseDTO;
import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.ReglaNegocioException;
import co.fluxis.store.repositories.ProductoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/productos")
public class ProductoController {

    private static List<Producto> productos;


    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {

        this.productoRepository = productoRepository;

    }


    @GetMapping
    public ResponseEntity<List<ConsultaProductoResposeDTO>> buscarProductos(
            @RequestParam(required = false) Integer codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) EstadoProducto estado,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Integer stockMin,
            @RequestParam(required = false) Integer stockMax
    ) {

        var productos = productoRepository.buscarPorFiltros(
                codigo,
                nombre,
                estado,
                precioMin,
                precioMax,
                stockMin,
                stockMax
        );


        var response = productos.stream()
                .map(p -> new ConsultaProductoResposeDTO(
                        p.getCodigo(),
                        p.getNombre(),
                        p.getPrecio(),
                        p.getStock(),
                        p.getEstado()
                )).toList();

        if (productos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }



    /*
    @GetMapping
    public ResponseEntity<List<ConsultaProductoResposeDTO>> consultar() {

        var productos = productoRepository.listar();

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


    */

    @PostMapping
    public ResponseEntity<ProductoCreadoResponseDTO> registrar(@Valid @RequestBody CrearProductoRequestDTO dto) {


        var nombreExiste = productoRepository.existePorNombre(dto.nombre());

        if (nombreExiste) {

            return ResponseEntity.unprocessableContent().build();
        }

        Producto nuevoProducto = null;

        try {
            nuevoProducto = new Producto(dto.nombre(), dto.precio(), dto.stock());
            productoRepository.registrar(nuevoProducto);

        } catch (ReglaNegocioException e) {

            return ResponseEntity.unprocessableContent().build();

        } catch (Exception e) {

            return ResponseEntity.badRequest().build();
        }

        var productoCreado = new ProductoCreadoResponseDTO(nuevoProducto.getCodigo(), nuevoProducto.getNombre(), nuevoProducto.getPrecio());

        return new ResponseEntity<>(productoCreado, HttpStatus.CREATED);

    }


    @GetMapping("/{codigo}")
    public ResponseEntity<ConsultaProductoResposeDTO> consultar(@PathVariable int codigo) {


        var producto = productoRepository.consultarPorCodigo(codigo);

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

        var descontinuado = productoRepository.descontinuar(codigo);

        if (descontinuado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<Void> actualizarProducto(
            @PathVariable int codigo,
            @RequestBody ActualizarProductoRequestDTO request
    ) {

        var actualizado = productoRepository.actualizar(codigo, request.precio(), request.stock());

        if (actualizado) {

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.badRequest().build();


    }


}
