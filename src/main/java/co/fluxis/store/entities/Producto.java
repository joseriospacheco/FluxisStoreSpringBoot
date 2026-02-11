package co.fluxis.store.entities;

import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.ReglaNegocioException;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;
import java.util.Random;

@Getter
public class Producto {

    private final int codigo;
    @Setter
    private String nombre;
    private double precio;
    private int stock;
    private EstadoProducto estado;
    public static final double PRECIO_MINIMO = 500.0;

    public Producto(String nombre, double precio, int stock) {

        if (Objects.isNull(nombre) || nombre.isBlank())
            throw new ReglaNegocioException("El nombre no puede ser nulo ni estar en blanco");

        if (precio <= PRECIO_MINIMO)
            throw new ReglaNegocioException("El precio del producto no puede ser menor a "+PRECIO_MINIMO+" COP");

        if (stock < 0)
            throw new ReglaNegocioException("El stock del producto no puede ser menor que cero");

        this.codigo = new Random().nextInt(900000) + 100000;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.estado = EstadoProducto.DISPONIBLE;
    }
    public void setPrecio(double precio) {

        if (precio <= PRECIO_MINIMO)
            throw new ReglaNegocioException("El precio del producto " + nombre + " no puede ser menor o igual que cero");

        this.precio = precio;
    }
    public void descontinuar() {
        this.estado = EstadoProducto.DESCONTINUADO;
    }
    public void agregarAlStock(int cantidad) {

        if (cantidad <= 0)
            throw new ReglaNegocioException("La cantidad para agregar al stock del producto " + nombre + " no puede ser menor o igual que cero");

        this.stock += cantidad;

        if (this.stock>0)
            this.estado = EstadoProducto.DISPONIBLE;
    }
    public void removerDelStock(int cantidad) {

        if (cantidad <= 0)
            throw new ReglaNegocioException("La cantidad a remover debe ser mayor que cero");

        if (this.stock < cantidad)
            throw new ReglaNegocioException("La cantidad a remover del stock del producto " + nombre + " no puede ser mayor al stock actual (" + stock + ")");

        this.stock -= cantidad;

        if (this.stock == 0)
            this.estado = EstadoProducto.AGOTADO;
    }
}
