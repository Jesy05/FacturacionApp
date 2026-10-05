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

    // Lista que muestra solo las categorías que coinciden con la búsqueda
    private final FilteredList<Categoria> categoriasFiltradas = new FilteredList<>(categorias, c -> true);

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colActiva.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isActiva()));
        colActiva.setCellFactory(CheckBoxTableCell.forTableColumn(colActiva));

        // SortedList permite seguir ordenando la tabla al hacer clic en las columnas
        SortedList<Categoria> categoriasOrdenadas = new SortedList<>(categoriasFiltradas);
        categoriasOrdenadas.comparatorProperty().bind(tablaCategorias.comparatorProperty());
        tablaCategorias.setItems(categoriasOrdenadas);

        // Cada vez que se escribe en el buscador se vuelve a filtrar la tabla
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
            Mensajes.mostrarError("Error de base de datos", "No se pudo guardar: " + e.getMessage());
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

        // Se valida nuevamente el nombre antes de ejecutar el UPDATE
        if (!validarCategoria()) {
            return;
        }

        String nombre = txtNombre.getText().trim();

        try {
            // Se excluye la categoría seleccionada para que su propio nombre no cuente como duplicado
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
            Mensajes.mostrarError("Error de base de datos", "No se pudo actualizar: " + e.getMessage());
        }

        // Se recarga para que la tabla muestre lo que realmente quedó en la base de datos
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
            // Integridad referencial: se revisa antes del DELETE en vez de esperar el error de la llave foránea
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
            Mensajes.mostrarError("Error de base de datos", "No se pudo eliminar: " + e.getMessage());
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
            Mensajes.mostrarError("Error de base de datos", "No se pudieron cargar las categorías: " + e.getMessage());
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

    // trim() hace que un nombre con solo espacios quede vacío y también se rechace
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
