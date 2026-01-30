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
    public ResponseEntity<List<ConsultaProductoResposeDTO>> consultar() {

        var productos = productoRepository.findAll();

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

    @PostMapping
    public ResponseEntity<ProductoCreadoResponseDTO> crear(@Valid @RequestBody CrearProductoRequestDTO dto) {

        /*
        // Validar nombre duplicado
        var nombreExiste = productos.stream()
                .anyMatch(p -> p.getDescripcion().equalsIgnoreCase(dto.nombre()));

        if (nombreExiste) {
            //throw new ReglaNegocioException("Ya existe un producto con el nombre: " + dto.nombre());
            return ResponseEntity.unprocessableContent().build();
        }


        */

        Producto nuevoProducto = null;

        try {
            nuevoProducto = new Producto(dto.nombre(), dto.precio(), dto.stock());

            productoRepository.save(nuevoProducto);


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
    public ResponseEntity<Void> descontinuarProducto(@PathVariable long codigo) {

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


}
