package ni.edu.uam.facturacion.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import ni.edu.uam.facturacion.dao.CategoriaDAO;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.util.SceneManager;

import java.io.IOException;
import java.sql.SQLException;

public class CategoriaController {

    @FXML
    private TextField txtNombre;

    @FXML
    private CheckBox chkActiva;

    @FXML
    private TableView<Categoria> tablaCategorias;

    @FXML
    private TableColumn<Categoria, Number> colId;

    @FXML
    private TableColumn<Categoria, String> colNombre;

    @FXML
    private TableColumn<Categoria, Boolean> colActiva;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();

    private Categoria categoriaSeleccionada;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colActiva.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isActiva()));
        colActiva.setCellFactory(CheckBoxTableCell.forTableColumn(colActiva));

        tablaCategorias.setItems(categorias);

        tablaCategorias.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nueva) -> seleccionar(nueva));

        cargarCategorias();
    }

    @FXML
    private void guardar() {
        if (!validar()) {
            return;
        }

        Categoria categoria = new Categoria(
                null,
                txtNombre.getText().trim(),
                chkActiva.isSelected()
        );

        try {
            categoriaDAO.guardar(categoria);
            mostrarMensaje(Alert.AlertType.INFORMATION, "Categoría guardada correctamente.");
            limpiar();
            cargarCategorias();
        } catch (SQLException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        if (categoriaSeleccionada == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Seleccione una categoría de la tabla.");
            return;
        }

        if (!validar()) {
            return;
        }

        categoriaSeleccionada.setNombre(txtNombre.getText().trim());
        categoriaSeleccionada.setActiva(chkActiva.isSelected());

        try {
            categoriaDAO.actualizar(categoriaSeleccionada);
            mostrarMensaje(Alert.AlertType.INFORMATION, "Categoría actualizada correctamente.");
            limpiar();
            cargarCategorias();
        } catch (SQLException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo actualizar: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (categoriaSeleccionada == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Seleccione una categoría de la tabla.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la categoría \"" + categoriaSeleccionada.getNombre() + "\"?");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            categoriaDAO.eliminar(categoriaSeleccionada.getId());
            limpiar();
            cargarCategorias();
        } catch (SQLException e) {
            // La llave foránea impide borrar una categoría que tiene productos
            mostrarMensaje(Alert.AlertType.ERROR,
                    "No se puede eliminar porque tiene productos asociados. "
                            + "Desmarque \"Activa\" para desactivarla.");
        }
    }

    @FXML
    private void limpiar() {
        categoriaSeleccionada = null;
        txtNombre.clear();
        chkActiva.setSelected(true);
        tablaCategorias.getSelectionModel().clearSelection();
    }

    @FXML
    private void volver() throws IOException {
        SceneManager.switchTo("/ni/edu/uam/facturacion/fxml/menu-principal.fxml");
    }

    private void seleccionar(Categoria categoria) {
        if (categoria == null) {
            return;
        }

        categoriaSeleccionada = categoria;
        txtNombre.setText(categoria.getNombre());
        chkActiva.setSelected(categoria.isActiva());
    }

    private void cargarCategorias() {
        try {
            categorias.setAll(categoriaDAO.listar());
        } catch (SQLException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudieron cargar las categorías: " + e.getMessage());
        }
    }

    private boolean validar() {
        if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
            mostrarMensaje(Alert.AlertType.WARNING, "El nombre es obligatorio.");
            return false;
        }
        return true;
    }

    private void mostrarMensaje(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo, mensaje);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
