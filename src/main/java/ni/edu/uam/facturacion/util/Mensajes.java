package ni.edu.uam.facturacion.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.SQLException;

/**
 * Cuadros de diálogo para informar al usuario lo que ocurrió en una operación.
 */
public final class Mensajes {

    private Mensajes() {
    }

    public static void mostrarExito(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    public static void mostrarAdvertencia(String titulo, String mensaje) {
        mostrar(Alert.AlertType.WARNING, titulo, mensaje);
    }

    public static void mostrarError(String titulo, String mensaje) {
        mostrar(Alert.AlertType.ERROR, titulo, mensaje);
    }

    /**
     * Informa un error de base de datos con un mensaje comprensible para el usuario.
     * El detalle técnico de la SQLException se escribe en la consola, no en la ventana.
     */
    public static void mostrarErrorBaseDatos(String mensaje, SQLException e) {
        System.err.println("[SQLException] " + mensaje + " -> " + e.getMessage());
        mostrarError("Error de base de datos", mensaje);
    }

    /**
     * Pide confirmación al usuario y devuelve true solo si presiona Aceptar.
     */
    public static boolean confirmar(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mensaje);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo, mensaje);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
