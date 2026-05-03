package co.fluxis.store.services;

import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.dtos.responses.ProductoRespose;
import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.EntityNotFoundException;
import co.fluxis.store.exceptions.ReglaNegocioException;
import co.fluxis.store.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductoService {


    @Autowired
    private ProductoRepository productoRepository;


    public List<ProductoRespose> listar() {

        List<Producto> productos = productoRepository.listar();

        return productos.stream()
                .map(p -> new ProductoRespose(
                        p.getCodigo(),
                        p.getNombre(),
                        p.getPrecio(),
                        p.getStock(),
                        p.getEstado()
                )).toList();
    }



    public List<ProductoRespose> buscarProductos(
            int codigo,
            String nombre,
            EstadoProducto estado,
            Double precioMin,
            Double precioMax,
            Integer stockMin,
            Integer stockMax
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

        return productos.stream()
                .map(p -> new ProductoRespose(
                        p.getCodigo(),
                        p.getNombre(),
                        p.getPrecio(),
                        p.getStock(),
                        p.getEstado()
                )).toList();
    }

    public ProductoRespose registrarProducto(CrearProductoRequest dto)  {

        if (productoRepository.existe(dto.nombre()))
            throw new ReglaNegocioException("Ya existe un producto con ese nombre");

        if (dto.precio() <= 0)
            throw new ReglaNegocioException("El precio del producto no puede ser menor o igual a cero");

        if (dto.stock() < 0)
            throw new ReglaNegocioException("El stock del producto no puede ser menor que cero");

        //Mapeo Dto - Entidad
        Producto nuevoProducto = new Producto(dto.nombre(),dto.precio(),dto.stock());

        productoRepository.registrar(nuevoProducto);

        //Mapeo  Entidad - Dto
        return new ProductoRespose(
                nuevoProducto.getCodigo(),
                nuevoProducto.getNombre(),
                nuevoProducto.getPrecio(),
                nuevoProducto.getStock(),
                nuevoProducto.getEstado()
        );
    }

    public Optional<ProductoRespose> consultarPorCodigo(int codigo) {

        Optional<Producto> producto = productoRepository.consultarPorCodigo(codigo);

        return producto.map(p -> new ProductoRespose(
                p.getCodigo(),
                p.getNombre(),
                p.getPrecio(),
                p.getStock(),
                p.getEstado()
        ));
    }

    public boolean descontinuarProducto(int codigo) {
        return productoRepository.descontinuar(codigo);
    }

    public boolean actualizar(int codigo, ActualizarProductoRequest request) {

        if (!productoRepository.existe(codigo))
            throw new EntityNotFoundException("Producto no encontrado");

        if (Objects.isNull(request.nombre()) || request.nombre().isBlank())
            throw new ReglaNegocioException("Ingrese el un nombre valido");

        if (request.precio()<0)
            throw new ReglaNegocioException("El precio de producto no pueder ser negativo");

        return productoRepository.actualizar(codigo, request.nombre(), request.precio());
    }
}