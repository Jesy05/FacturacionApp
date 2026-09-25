package ni.edu.uam.facturacion.util;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConexion {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            System.out.println("Conexión exitosa a: "
                    + connection.getCatalog());

        } catch (SQLException e) {

            System.out.println("Error de conexión: "
                    + e.getMessage());
        }
    }
}
