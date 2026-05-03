package co.fluxis.store.repositories;

import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.EntityNotFoundException;
import co.fluxis.store.exceptions.PersistenseException;
import co.fluxis.store.exceptions.ReglaNegocioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductoRepository {

    @Autowired
    private DataSource dataSource;
    private String sql;

    public boolean actualizar(int codigo,String nombre, double precio) {

        sql = "UPDATE productos SET  nombre = ?, precio = ?, ultima_fecha_actualizacion=? WHERE codigo = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setDouble(2, precio);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(4, codigo);

            return pstmt.executeUpdate()>0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar precio y stock del producto con codigo: " + codigo, e);
        }
    }



    public boolean descontinuar(int codigo) {

        sql = "UPDATE productos SET estado = ? WHERE codigo = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, EstadoProducto.DESCONTINUADO.name());
            pstmt.setInt(2, codigo);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al descontinuar el producto con codigo: " + codigo, e);
        }
    }



    public List<Producto> listar() {
        sql = "SELECT * FROM productos";
        List<Producto> productos = new ArrayList<>();
        Producto producto;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                //producto = new Producto(rs.getString("nombre"), rs.getDouble("precio"), rs.getInt("stock"));

                producto = new Producto();
                producto.setCodigo(rs.getInt("codigo"));
                producto.setNombre(rs.getString("nombre"));
                producto.setPrecio(rs.getDouble("precio"));
                producto.setStock(rs.getInt("stock"));
                producto.setEstado(EstadoProducto.valueOf(rs.getString("estado")));

                Timestamp ts = rs.getTimestamp("ultima_fecha_actualizacion");
                LocalDateTime fechaActualizacion = ts != null ? ts.toLocalDateTime() : null;
                producto.setFechaActualizacion(fechaActualizacion);

                productos.add(producto);
            }

        } catch (SQLException  e) {
            throw new RuntimeException("Error al obtener productos: "+e.getMessage());
        }

        return productos;
    }


    public boolean registrar(Producto producto) {

        sql = "INSERT INTO productos (codigo, nombre, precio, stock,fecha_creacion) VALUES (?, ?, ?, ?,?)";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setInt(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setDouble(3, producto.getPrecio());
            pstmt.setInt(4, producto.getStock());
            pstmt.setTimestamp(5, Timestamp.valueOf(producto.getFechaCreacion()));

            return pstmt.execute();

        } catch (SQLException e) {
            throw new PersistenseException("Error al guardar producto");
        }
    }


    public Producto consultarPorCodigo1(int codigo) {

        sql = "SELECT codigo, nombre, precio, stock,estado FROM productos WHERE codigo = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, codigo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Producto producto = new Producto();
                    producto.setCodigo(rs.getInt("codigo"));
                    producto.setNombre(rs.getString("nombre"));
                    producto.setPrecio(rs.getDouble("precio"));
                    producto.setStock(rs.getInt("stock"));
                    producto.setEstado(EstadoProducto.valueOf(rs.getString("estado")));
                    return producto;

                }else
                    throw new EntityNotFoundException("Error al consultar producto por codigo");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar producto por codigo");
        }

    }




    public Optional<Producto> consultarPorCodigo(int codigo) {

        sql = "SELECT codigo, nombre, precio, stock,estado FROM productos WHERE codigo = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, codigo);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    Producto producto = new Producto();
                    producto.setCodigo(rs.getInt("codigo"));
                    producto.setNombre(rs.getString("nombre"));
                    producto.setPrecio(rs.getDouble("precio"));
                    producto.setStock(rs.getInt("stock"));
                    producto.setEstado(EstadoProducto.valueOf(rs.getString("estado")));

                    return Optional.of(producto);
                }
            }

        } catch (SQLException  e) {
            throw new RuntimeException("Error al consultar producto por codigo");
        }
        return Optional.empty();
    }

    public List<Producto> buscarPorFiltros(
            Integer codigo,
            String nombre,
            EstadoProducto estado,
            Double precioMin,
            Double precioMax,
            Integer stockMin,
            Integer stockMax
    ) {

        StringBuilder sql = new StringBuilder(
                "SELECT codigo, nombre, precio, stock, estado FROM productos WHERE 1=1"
        );

        List<Object> parametros = new ArrayList<>();

        if (codigo != null) {
            sql.append(" AND codigo = ?");
            parametros.add(codigo);
        }

        if (nombre != null && !nombre.isBlank()) {
            sql.append(" AND LOWER(nombre) LIKE LOWER(?)");
            parametros.add("%" + nombre + "%");
        }

        if (estado != null) {
            sql.append(" AND estado = ?");
            parametros.add(estado.name());
        }

        if (precioMin != null) {
            sql.append(" AND precio >= ?");
            parametros.add(precioMin);
        }

        if (precioMax != null) {
            sql.append(" AND precio <= ?");
            parametros.add(precioMax);
        }

        if (stockMin != null) {
            sql.append(" AND stock >= ?");
            parametros.add(stockMin);
        }

        if (stockMax != null) {
            sql.append(" AND stock <= ?");
            parametros.add(stockMax);
        }

        List<Producto> productos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setCodigo(rs.getInt("codigo"));
                    producto.setNombre(rs.getString("nombre"));
                    producto.setPrecio(rs.getDouble("precio"));
                    producto.setStock(rs.getInt("stock"));
                    producto.setEstado(
                            EstadoProducto.valueOf(rs.getString("estado"))
                    );

                    productos.add(producto);
                }
            }

        } catch (SQLException | ReglaNegocioException e) {
            throw new RuntimeException("Error al buscar productos por filtros", e);
        }

        return productos;
    }



    public List<Producto> buscarPorFiltros1(
            String nombre,
            EstadoProducto estado,
            Double precioMin,
            Double precioMax,
            Integer stockMin,
            Integer stockMax
    ) {

        StringBuilder sql = new StringBuilder(
                "SELECT codigo, nombre, precio, stock, estado FROM productos WHERE 1=1"
        );

        List<Object> parametros = new ArrayList<>();


        if (nombre != null && !nombre.isBlank()) {
            sql.append(" AND LOWER(nombre) LIKE LOWER(?)");
            parametros.add("%" + nombre + "%");
        }

        if (estado != null) {
            sql.append(" AND estado = ?");
            parametros.add(estado.name());
        }

        if (precioMin != null) {
            sql.append(" AND precio >= ?");
            parametros.add(precioMin);
        }

        if (precioMax != null) {
            sql.append(" AND precio <= ?");
            parametros.add(precioMax);
        }

        if (stockMin != null) {
            sql.append(" AND stock >= ?");
            parametros.add(stockMin);
        }

        if (stockMax != null) {
            sql.append(" AND stock <= ?");
            parametros.add(stockMax);
        }

        List<Producto> productos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setCodigo(rs.getInt("codigo"));
                    producto.setNombre(rs.getString("nombre"));
                    producto.setPrecio(rs.getDouble("precio"));
                    producto.setStock(rs.getInt("stock"));
                    producto.setEstado(EstadoProducto.valueOf(rs.getString("estado")));

                    productos.add(producto);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar productos por filtros");
        }

        return productos;
    }


    public boolean existe(String nombre) {

        sql = "SELECT 1 FROM productos WHERE LOWER(nombre) = LOWER(?) LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al validar producto por nombre", e);
        }
    }

    public boolean existe(int codigo) {
        sql = "SELECT 1 FROM productos WHERE codigo=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, codigo);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Consultar el producto");
        }
    }

}
