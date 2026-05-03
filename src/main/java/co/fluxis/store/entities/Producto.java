package co.fluxis.store.entities;

import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.ReglaNegocioException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;


public class Producto {

    private int codigo;
    private String nombre;
    private double precio;
    private int stock;
    private EstadoProducto estado;
    private LocalTime fechaRegistro;
    private LocalDateTime  ultimaFechaActualizacion;

    public Producto() {
        this.codigo = 0;
        this.nombre = "";
        this.precio = 0;
        this.stock = 0;
    }

    public Producto(String nombre, double precio, int stock)  {

        if (precio <= 0) {
            throw new ReglaNegocioException("El precio del producto no puede ser menor o igual a cero");
        }

        if (stock < 0) {
            throw new ReglaNegocioException("El stock del producto no puede ser menor que cero");
        }

        if (Objects.isNull(nombre) || nombre.isBlank())
            throw new ReglaNegocioException("Ingrese el un nombre valido");

        this.codigo = generarCodigo();
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.fechaRegistro = LocalTime.now();
        this.estado = EstadoProducto.DISPONIBLE;

    }


    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }


    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public void setPrecio(double precio)  {

        if (precio <= 0) {

            throw new ReglaNegocioException("La precio del producto " + nombre + "+ no puede ser menor o igual que cero");
        }

        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public EstadoProducto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProducto estado) {
        this.estado = estado;
    }


    public void agregarAlStock(int cantidad)  {

        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad para agrgar al stock del producto " + nombre + "+ no puede ser menor o igual que cero");
        }
        this.stock += cantidad;
    }

    public void removerDelStock(int cantidad)  {

        if (this.stock >= cantidad) {
            this.stock -= cantidad;
        } else {
            throw new ReglaNegocioException("La cantidad a remover del stock del producto " + nombre + " no puede ser mayor a stock actual (" + stock + ")");
        }

        if (this.stock == 0) {
            estado = EstadoProducto.AGOTADO;
        }
    }

    private int generarCodigo() {
        return new Random().nextInt(900000) + 100000;
    }

    @Override
    public String toString() {
        return "ID: " + codigo + " | Nombre: " + nombre + " | Precio: $" + String.format("%,.0f", precio) + " | Stock: " + stock + " | Estado: " + estado;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getUltimaFechaActualizacion() {
        return ultimaFechaActualizacion;
    }

    public LocalDateTime getFechaCreacion() {
        return ultimaFechaActualizacion;
    }

    public void setUltimaFechaActualizacion(LocalDateTime ultimaFechaActualizacion) {
        this.ultimaFechaActualizacion = ultimaFechaActualizacion;
    }
}
