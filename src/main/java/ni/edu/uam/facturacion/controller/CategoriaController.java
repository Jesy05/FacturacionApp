package ni.edu.uam.facturacion.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import ni.edu.uam.facturacion.dao.CategoriaDAO;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.util.Mensajes;
import ni.edu.uam.facturacion.util.SceneManager;

import java.io.IOException;
import java.sql.SQLException;

public class CategoriaController {

    @FXML
    private TextField txtNombre;

    @FXML
    private CheckBox chkActiva;

    @FXML
    private TextField txtBuscar;

    @FXML
    private Label lblResultados;

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

    private final FilteredList<Categoria> categoriasFiltradas = new FilteredList<>(categorias, c -> true);

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colActiva.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isActiva()));
        colActiva.setCellFactory(CheckBoxTableCell.forTableColumn(colActiva));

        SortedList<Categoria> categoriasOrdenadas = new SortedList<>(categoriasFiltradas);
        categoriasOrdenadas.comparatorProperty().bind(tablaCategorias.comparatorProperty());
        tablaCategorias.setItems(categoriasOrdenadas);

        txtBuscar.textProperty().addListener((obs, anterior, texto) -> filtrar(texto));

        tablaCategorias.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nueva) -> seleccionar(nueva));

        cargarCategorias();
    }

    @FXML
    private void guardar() {
        if (!validarCategoria()) {
            return;
        }

        String nombre = txtNombre.getText().trim();

        try {
            if (categoriaDAO.existeNombre(nombre)) {
                Mensajes.mostrarAdvertencia("Categoría duplicada",
                        "Ya existe una categoría con ese nombre.");
                txtNombre.requestFocus();
                return;
            }

            Categoria categoria = new Categoria(null, nombre, chkActiva.isSelected());

            categoriaDAO.guardar(categoria);
            Mensajes.mostrarExito("Categoría registrada", "La categoría se guardó correctamente.");
            limpiar();
            cargarCategorias();
        } catch (SQLException e) {
            Mensajes.mostrarErrorBaseDatos("No fue posible registrar la categoría.", e);
        }
    }

    @FXML
    private void actualizar() {
        Categoria seleccionada = tablaCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            Mensajes.mostrarAdvertencia("Seleccione una categoría",
                    "Debe seleccionar la categoría que desea actualizar.");
            return;
        }

        if (!validarCategoria()) {
            return;
        }

        String nombre = txtNombre.getText().trim();

        try {
            if (categoriaDAO.existeNombre(nombre, seleccionada.getId())) {
                Mensajes.mostrarAdvertencia("Categoría duplicada",
                        "Ya existe otra categoría con ese nombre.");
                txtNombre.requestFocus();
                return;
            }

            seleccionada.setNombre(nombre);
            seleccionada.setActiva(chkActiva.isSelected());

            categoriaDAO.actualizar(seleccionada);
            Mensajes.mostrarExito("Categoría actualizada", "La categoría se actualizó correctamente.");
            limpiar();
        } catch (SQLException e) {
            Mensajes.mostrarErrorBaseDatos("No fue posible actualizar la categoría.", e);
        }

        cargarCategorias();
    }

    @FXML
    private void eliminar() {
        Categoria seleccionada = tablaCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            Mensajes.mostrarAdvertencia("Seleccione una categoría",
                    "Debe seleccionar la categoría que desea eliminar.");
            return;
        }

        if (!Mensajes.confirmar("Eliminar categoría",
                "¿Eliminar la categoría \"" + seleccionada.getNombre() + "\"?")) {
            return;
        }

        try {
            if (categoriaDAO.tieneProductos(seleccionada.getId())) {
                Mensajes.mostrarAdvertencia("No se puede eliminar",
                        "No puede eliminar la categoría porque tiene productos asociados. "
                                + "Puede desmarcar \"Activa\" para desactivarla.");
                return;
            }

            categoriaDAO.eliminar(seleccionada.getId());
            limpiar();
            cargarCategorias();
        } catch (SQLException e) {
            Mensajes.mostrarErrorBaseDatos("No fue posible eliminar la categoría.", e);
        }
    }

    @FXML
    private void limpiar() {
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

        txtNombre.setText(categoria.getNombre());
        chkActiva.setSelected(categoria.isActiva());
    }

    private void cargarCategorias() {
        try {
            categorias.setAll(categoriaDAO.listar());
        } catch (SQLException e) {
            Mensajes.mostrarErrorBaseDatos("No fue posible cargar las categorías. Verifique la conexión con la base de datos.", e);
        }
        actualizarResultados();
    }

    private void filtrar(String texto) {
        String busqueda = texto == null ? "" : texto.trim().toLowerCase();

        categoriasFiltradas.setPredicate(categoria ->
                busqueda.isEmpty() || categoria.getNombre().toLowerCase().contains(busqueda));

        actualizarResultados();
    }

    private void actualizarResultados() {
        lblResultados.setText(categoriasFiltradas.size() + " de " + categorias.size() + " categorías");
    }

    private boolean validarCategoria() {
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            Mensajes.mostrarError("Validación", "El nombre de la categoría es obligatorio.");
            txtNombre.requestFocus();
            return false;
        }

        return true;
    }
}
