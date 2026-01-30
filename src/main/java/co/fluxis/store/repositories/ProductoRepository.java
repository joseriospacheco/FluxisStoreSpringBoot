package co.fluxis.store.repositories;

import co.fluxis.store.entities.Producto;
import co.fluxis.store.enums.EstadoProducto;
import co.fluxis.store.exceptions.ReglaNegocioException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductoRepository {

    private final DataSource dataSource; // ← Spring lo inyecta automáticamente

    public ProductoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }



    public boolean actualizar(int codigo, double precio, int stock) {

        String sql = "UPDATE productos SET precio = ?, stock = ? WHERE codigo = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, precio);
            pstmt.setInt(2, stock);
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

                productos.add(producto);
            }

        } catch (SQLException | ReglaNegocioException e) {
            throw new RuntimeException("Error al obtener productos: " + e.getMessage(), e);
        }

        return productos;
    }

    public Producto registrar(Producto producto) {

        String sql = "INSERT INTO productos (codigo, nombre, precio, stock) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setDouble(3, producto.getPrecio());
            pstmt.setInt(4, producto.getStock());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Error al crear producto, ninguna fila afectada.");
            }

            return producto;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar producto", e);
        }
    }


    public Optional<Producto> consultarPorCodigo(int codigo) {

        String sql = "SELECT codigo, nombre, precio, stock,estado FROM productos WHERE codigo = ?";

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

        } catch (SQLException | ReglaNegocioException e) {
            throw new RuntimeException("Error al consultar producto por codigo", e);
        }
        return Optional.empty();
    }

}
