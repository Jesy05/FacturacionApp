package ni.edu.uam.facturacion.dao;

import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public void guardar(Categoria categoria) throws SQLException {

        String sql = """
                INSERT INTO categoria (nombre, activa)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());

            ps.executeUpdate();
        }
    }

    public List<Categoria> listar() throws SQLException {

        String sql = """
                SELECT id, nombre, activa
                FROM categoria
                ORDER BY id
                """;

        List<Categoria> categorias = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categorias.add(mapear(rs));
            }
        }

        return categorias;
    }

    public List<Categoria> listarActivas() throws SQLException {

        String sql = """
                SELECT id, nombre, activa
                FROM categoria
                WHERE activa = TRUE
                ORDER BY nombre
                """;

        List<Categoria> categorias = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categorias.add(mapear(rs));
            }
        }

        return categorias;
    }

    public Categoria buscar(int id) throws SQLException {

        String sql = """
                SELECT id, nombre, activa
                FROM categoria
                WHERE id = ?
                """;

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

    public void actualizar(Categoria categoria) throws SQLException {

        String sql = """
                UPDATE categoria
                SET nombre = ?, activa = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());
            ps.setInt(3, categoria.getId());

            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {

        String sql = """
                DELETE FROM categoria
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();
        }
    }


    public boolean existeNombre(String nombre) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM categoria
                WHERE LOWER(nombre) = LOWER(?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    public boolean existeNombre(String nombre, int idExcluido) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM categoria
                WHERE LOWER(nombre) = LOWER(?)
                  AND id <> ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idExcluido);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }


    public boolean tieneProductos(int categoriaId) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM producto
                WHERE categoria_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, categoriaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private Categoria mapear(ResultSet rs) throws SQLException {

        return new Categoria(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getBoolean("activa")
        );
    }
}
