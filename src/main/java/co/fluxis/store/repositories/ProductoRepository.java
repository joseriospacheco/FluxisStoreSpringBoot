package co.fluxis.store.repositories;

import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.ReglaNegocioException;
import co.fluxis.store.mappers.PreparedStatementMapper;
import co.fluxis.store.mappers.ResultSetMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductoRepository {
    private final DataSource dataSource; // ← Spring lo inyecta automáticamente
    private final ResultSetMapper<Producto> productoMapper;
    private final PreparedStatementMapper<Producto> psMapper;

    public ProductoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.productoMapper = new ResultSetMapper<>(Producto.class);
        this.psMapper = new PreparedStatementMapper<>(Producto.class);
    }

    public boolean actualizar(int codigo,String nombre, double precio) {

        String sql = "UPDATE productos SET  nombre = ?, precio = ? WHERE codigo = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setDouble(2, precio);
            pstmt.setInt(3, codigo);

            int filasAfectadas = pstmt.executeUpdate();

            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar precio y stock del producto con codigo: " + codigo, e);
        }
    }



    public boolean descontinuar(int codigo) {

        String sql = "UPDATE productos SET estado = ? WHERE codigo = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, EstadoProducto.DESCONTINUADO.name());
            pstmt.setInt(2, codigo);

            int filasAfectadas = pstmt.executeUpdate();

            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al descontinuar el producto con codigo: " + codigo, e);
        }
    }



    public List<Producto> listar() {
        String sql = "SELECT * FROM productos";
        List<Producto> productos = new ArrayList<>();
        Producto producto;

        try {
             Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);

            return productoMapper.mapResultSet(rs);

            /*
            while (rs.next()) {

                //producto = new Producto(rs.getString("nombre"), rs.getDouble("precio"), rs.getInt("stock"));

                producto = new Producto();
                producto.setCodigo(rs.getInt("codigo"));
                producto.setNombre(rs.getString("nombre"));
                producto.setPrecio(rs.getDouble("precio"));
                producto.setStock(rs.getInt("stock"));
                producto.setEstado(EstadoProducto.valueOf(rs.getString("estado")));

                productos.add(producto);
            }
                    return productos;
             */

        } catch (SQLException | ReglaNegocioException e) {
            throw new RuntimeException("Error al obtener productos: " + e.getMessage(), e);
        }


    }

    public Producto registrar(Producto producto) {

        String sql = "INSERT INTO productos (codigo, nombre, precio, stock, estado) VALUES (?, ?, ?, ?,?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {


            System.out.println(producto);

            /*
            pstmt.setInt(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setDouble(3, producto.getPrecio());
            pstmt.setInt(4, producto.getStock());
            pstmt.setString(5, producto.getEstado());
            */

            psMapper.mapToPreparedStatement(producto, pstmt);


            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Error al crear producto, ninguna fila afectada.");
            }

            return producto;

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    public Optional<Producto> consultarPorCodigo(int codigo) {

        String sql = "SELECT codigo, nombre, precio, stock,estado FROM productos WHERE codigo = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, codigo);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {


                    /*

                    Producto producto = new Producto();
                    producto.setCodigo(rs.getInt("codigo"));
                    producto.setNombre(rs.getString("nombre"));
                    producto.setPrecio(rs.getDouble("precio"));
                    producto.setStock(rs.getInt("stock"));
                    producto.setEstado(EstadoProducto.valueOf(rs.getString("estado")));

                    */

                    return Optional.of(productoMapper.mapRow(rs));

                }
                return Optional.empty();
            }

        } catch (SQLException | ReglaNegocioException e) {
            throw new RuntimeException("Error al consultar producto por codigo", e);
        }

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

                return productoMapper.mapResultSet(rs);

                /*

                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setCodigo(rs.getInt("codigo"));
                    producto.setNombre(rs.getString("nombre"));
                    producto.setPrecio(rs.getDouble("precio"));
                    producto.setStock(rs.getInt("stock"));
                    producto.setEstado(EstadoProducto.valueOf(rs.getString("estado")));
                    productos.add(producto);
                }

                */
            }

        } catch (SQLException | ReglaNegocioException e) {
            throw new RuntimeException("Error al buscar productos por filtros", e);
        }


    }



    public boolean existePorNombre(String nombre) {

        String sql = "SELECT 1 FROM productos WHERE LOWER(nombre) = LOWER(?) LIMIT 1";

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


}
