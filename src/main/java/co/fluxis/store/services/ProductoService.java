package co.fluxis.store.services;

import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.dtos.responses.ProductoRespose;
import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.ReglaNegocioException;
import co.fluxis.store.mappers.ProductoMapper;
import co.fluxis.store.repositories.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    public ProductoService(ProductoRepository productoRepository, ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
    }

    public List<ProductoRespose> buscarProductos(
            Integer codigo,
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

        return productoMapper.toResponseList(productos);
    }

    public ProductoRespose registrarProducto(CrearProductoRequest dto) throws ReglaNegocioException {
        // Validación de unicidad
        if (productoRepository.existePorNombre(dto.nombre())) {
            throw new ReglaNegocioException("Ya existe un producto con ese nombre");
        }


        /*
        Producto nuevoProducto = productoMapper.toEntity(dto);
        */

        Producto nuevoProducto = new Producto(dto.nombre(),dto.precio(),dto.stock());

        productoRepository.registrar(nuevoProducto);

        return productoMapper.toResponse(nuevoProducto);
    }

    public Optional<ProductoRespose> consultarPorCodigo(int codigo) {
        return productoRepository.consultarPorCodigo(codigo)
                .map(productoMapper::toResponse);
    }

    public boolean descontinuarProducto(int codigo) {
        return productoRepository.descontinuar(codigo);
    }

    public boolean actualizarProducto(int codigo, ActualizarProductoRequest request) {
        return productoRepository.actualizar(codigo, request.nombre(), request.precio());
    }
}