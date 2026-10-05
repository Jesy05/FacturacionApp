package ni.edu.uam.facturacion.dao;

import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.model.Producto;
import ni.edu.uam.facturacion.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private static final String SELECT_BASE = """
            SELECT p.id, p.codigo, p.nombre, p.precio_venta,
                   p.existencia, p.activo,
                   c.id AS categoria_id,
                   c.nombre AS categoria_nombre,
                   c.activa AS categoria_activa
            FROM producto p
            INNER JOIN categoria c
            ON p.categoria_id = c.id
            """;

    public void guardar(Producto producto) throws SQLException {

        String sql = """
                INSERT INTO producto
                (
                    codigo,
                    nombre,
                    categoria_id,
                    precio_venta,
                    existencia,
                    activo
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setBoolean(6, producto.isActivo());

            ps.executeUpdate();

            // Se asigna al objeto el id que generó PostgreSQL
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    producto.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Producto> listar() throws SQLException {

        String sql = SELECT_BASE + " ORDER BY p.id";

        List<Producto> productos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapear(rs));
            }
        }

        return productos;
    }

    public Producto buscar(int id) throws SQLException {

        String sql = SELECT_BASE + " WHERE p.id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }

        return null;
    }

    public void actualizar(Producto producto) throws SQLException {

        String sql = """
                UPDATE producto
                SET codigo = ?,
                    nombre = ?,
                    categoria_id = ?,
                    precio_venta = ?,
                    existencia = ?,
                    activo = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setBoolean(6, producto.isActivo());
            ps.setInt(7, producto.getId());

            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {

        String sql = """
                DELETE FROM producto
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();
        }
    }

    /**
     * Indica si ya existe un producto con ese código (P001 y p001 se consideran iguales).
     */
    public boolean existeCodigo(String codigo) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM producto
                WHERE LOWER(codigo) = LOWER(?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Igual que existeCodigo, pero sin tomar en cuenta el producto que se está
     * actualizando, para que conservar su propio código no cuente como duplicado.
     */
    public boolean existeCodigo(String codigo, int idExcluido) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM producto
                WHERE LOWER(codigo) = LOWER(?)
                  AND id <> ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setInt(2, idExcluido);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private Producto mapear(ResultSet rs) throws SQLException {

        Categoria categoria = new Categoria(
                rs.getInt("categoria_id"),
                rs.getString("categoria_nombre"),
                rs.getBoolean("categoria_activa")
        );

        return new Producto(
                rs.getInt("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                categoria,
                rs.getBigDecimal("precio_venta"),
                rs.getInt("existencia"),
                rs.getBoolean("activo")
        );
    }
}
