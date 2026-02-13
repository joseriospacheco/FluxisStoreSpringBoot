package co.fluxis.store.services;

import co.fluxis.store.Repositories.ObjectRepository;
import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.dtos.responses.ProductoRespose;
import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.EntidadNoEncontradaException;
import co.fluxis.store.exceptions.ReglaNegocioException;
import co.fluxis.store.mappers.ProductoMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {


    private final ObjectRepository<Producto> productoRepository;
    private  final ProductoMapper productoMapper;


    public ProductoService(ProductoMapper productoMapper) {
        this.productoMapper = productoMapper;

        productoRepository = new ObjectRepository<>("data/productos.data");


    }


    private boolean existeProductoConNombre(String nombre) {

        try {
            return productoRepository.getAll().stream()
                    .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public ProductoRespose registrar(CrearProductoRequest dto) {

        if (existeProductoConNombre(dto.nombre())) {
            throw new ReglaNegocioException("Ya existe un producto con el nombre: " + dto.nombre());
        }

        Producto producto = new Producto(
                dto.nombre(),
                dto.precio(),
                dto.stock()
        );

        try {

            productoRepository.add(producto);
           return  productoMapper.toRespose(producto);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public List<ProductoRespose> buscar(
            Integer codigo,
            String nombre,
            EstadoProducto estado,
            Double precioMin,
            Double precioMax,
            Integer stockMin,
            Integer stockMax
    ) {

        try {
            return productoRepository.getAll().stream()
                    .filter(p -> codigo == null || p.getCodigo() == codigo)
                    .filter(p -> nombre == null || p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .filter(p -> estado == null || p.getEstado() == estado)
                    .filter(p -> precioMin == null || p.getPrecio() >= precioMin)
                    .filter(p -> precioMax == null || p.getPrecio() <= precioMax)
                    .filter(p -> stockMin == null || p.getStock() >= stockMin)
                    .filter(p -> stockMax == null || p.getStock() <= stockMax)
                    .map(productoMapper::toRespose)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public Optional<ProductoRespose> buscarPorCodigo(int codigo) {
        try {
            return productoRepository.getAll().stream()
                    .filter(p -> p.getCodigo() == codigo)
                    .findFirst()
                    .map(productoMapper::toRespose);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private Optional<Producto> buscar(int codigo) {
        try {
            return productoRepository.getAll().stream()
                    .filter(p -> p.getCodigo() == codigo)
                    .findFirst();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean descontinuar(int codigo) {

        int indice = 0;
        try {
            indice = productoRepository.indexWhere(p -> p.getCodigo() == codigo);


            if (indice >= 0) {

                Producto p = productoRepository.getAll().get(indice);
                p.descontinuar();
                return productoRepository.update(p);

            } else {
                throw new EntidadNoEncontradaException("No se encontró el producto espeficicado");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean actualizar(int codigo, ActualizarProductoRequest dto) {

        try {
            int indice = productoRepository.indexWhere(p -> p.getCodigo() == codigo);

            if (indice >= 0) {

                Producto p = productoRepository.getAll().get(indice);
                p.setNombre(dto.nombre());
                p.setPrecio(dto.precio());
                return productoRepository.update(p);

            }else {

                throw new EntidadNoEncontradaException("No se encontró el producto espeficicado");
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}
