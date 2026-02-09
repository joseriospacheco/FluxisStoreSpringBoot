package co.fluxis.store.services;


import co.fluxis.store.Repositories.ObjectRepository;
import co.fluxis.store.dtos.requests.ActualizarProductoRequest;
import co.fluxis.store.dtos.requests.CrearProductoRequest;
import co.fluxis.store.dtos.responses.ConsultaProductoRespose;
import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.EntidadNoEncontradaException;
import co.fluxis.store.exceptions.ReglaNegocioException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


@Service
public class ProductoService {
    private static ObjectRepository<Producto> productoRepository;

    public ProductoService() {

        productoRepository = new ObjectRepository<Producto>("data/productos.dat");

    }


    private  boolean existeProductoConNombre(String nombre){


        try {

            return productoRepository.getAll().stream().anyMatch(p->p.getNombre().equalsIgnoreCase(nombre));

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }

    public Producto registrar(CrearProductoRequest dto) {

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

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //  productos.add(producto);
        return producto;
    }

    public List<ConsultaProductoRespose> buscar(
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
                    .map(p -> new ConsultaProductoRespose(
                            p.getCodigo(),
                            p.getNombre(),
                            p.getPrecio(),
                            p.getStock(),
                            p.getEstado()
                    ))
                    .toList();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private Optional<Producto> buscarEntidadPorCodigo(int codigo) {
        try {
            return productoRepository.getAll().stream()
                    .filter(p -> p.getCodigo() == codigo)
                    .findFirst();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<ConsultaProductoRespose> buscarPorCodigo(int codigo) {
        return buscarEntidadPorCodigo(codigo)
                .map(p -> new ConsultaProductoRespose(
                        p.getCodigo(),
                        p.getNombre(),
                        p.getPrecio(),
                        p.getStock(),
                        p.getEstado()
                ));
    }

    public boolean descontinuar(int codigo) {



        try {

            List<Producto> productos = productoRepository.getAll();

            Optional<Producto> encontrado = productoRepository.getAll().stream()
                    .filter(p -> p.getCodigo() == codigo)
                    .findFirst();

            if (encontrado.isEmpty())
                return false;

            int posicion = encontrado
                    .map(productos::indexOf)
                    .orElse(-1);

            System.out.println(posicion);

            encontrado.ifPresent(producto -> {
                producto.descontinuar();


                try {




                    /*
                    *
                    Producto p = productoRepository.getAll().get(posicion);
                    p.descontinuar();
                    productoRepository.update(p);
                    *
                    * */

                    productoRepository.update(posicion,producto);



                } catch (IOException e) {
                    throw new RuntimeException(e);
                }



            });

            return true;

        }catch (Exception e){

        }

        return false;


    }

    public void actualizar(int codigo, ActualizarProductoRequest dto) throws IOException, ClassNotFoundException {


        List<Producto> productos = productoRepository.getAll();

        Optional<Producto> encontrado = productos.stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst();

        int posicion = encontrado
                .map(productos::indexOf)
                .orElse(-1);

        System.out.println(posicion);

        encontrado.ifPresent(producto -> {
            producto.setNombre(dto.nombre());
            producto.setPrecio(dto.precio());

            try {


                if (existeProductoConNombre(dto.nombre())) {
                    throw new ReglaNegocioException("Ya existe un producto con el nombre: " + dto.nombre());
                }

                productoRepository.update(posicion,producto);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });






        /*
        int posicion2 = IntStream.range(0, productos.size())
                .filter(i -> productos.get(i).getCodigo() == codigo)
                .findFirst()
                .orElse(-1);

        */

        /*
        var producto = buscarEntidadPorCodigo(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("Producto no encontrado"));

        if (existeProductoConNombre(dto.nombre())) {
            throw new ReglaNegocioException("Ya existe un producto con el nombre: " + dto.nombre());
        }

        producto.setNombre(dto.nombre());
        producto.setPrecio(dto.precio());

        productoRepository.update(producto);


        */

    }

}
