package ni.edu.uam.facturacion.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import ni.edu.uam.facturacion.dao.CategoriaDAO;
import ni.edu.uam.facturacion.dao.ProductoDAO;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.model.Producto;
import ni.edu.uam.facturacion.util.SceneManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ProductoController {

    private static final String ESTADO_TODOS = "Todos";
    private static final String ESTADO_ACTIVOS = "Activos";
    private static final String ESTADO_INACTIVOS = "Inactivos";

    // Opción del filtro que representa "sin filtrar por categoría"
    private static final Categoria TODAS_LAS_CATEGORIAS =
            new Categoria(null, "Todas las categorías", true);

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private ComboBox<Categoria> cmbCategoria;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtExistencia;

    @FXML
    private TextField txtRutaImagen;

    @FXML
    private CheckBox chkActivo;

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> cmbFiltroEstado;

    @FXML
    private ComboBox<Categoria> cmbFiltroCategoria;

    @FXML
    private Label lblResultados;

    @FXML
    private TableView<Producto> tablaProductos;

    @FXML
    private TableColumn<Producto, Number> colId;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, String> colCategoria;

    @FXML
    private TableColumn<Producto, BigDecimal> colPrecio;

    @FXML
    private TableColumn<Producto, Number> colExistencia;

    @FXML
    private TableColumn<Producto, Boolean> colActivo;

    private final ProductoDAO productoDAO = new ProductoDAO();

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    // Lista original: conserva todos los productos
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    // Lista filtrada: solo decide cuáles productos se muestran
    private final FilteredList<Producto> productosFiltrados = new FilteredList<>(productos, p -> true);

    private Producto productoSeleccionado;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colCategoria.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoria().getNombre()));
        colPrecio.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrecioVenta()));
        colExistencia.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getExistencia()));
        colActivo.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isActivo()));
        colActivo.setCellFactory(CheckBoxTableCell.forTableColumn(colActivo));

        // ObservableList → FilteredList → TableView
        // (SortedList permite seguir ordenando al hacer clic en las columnas)
        SortedList<Producto> productosOrdenados = new SortedList<>(productosFiltrados);
        productosOrdenados.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(productosOrdenados);

        tablaProductos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nuevo) -> seleccionar(nuevo));

        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                ESTADO_TODOS, ESTADO_ACTIVOS, ESTADO_INACTIVOS));
        cmbFiltroEstado.setValue(ESTADO_TODOS);

        // Los filtros se aplican en cuanto cambian, junto con la búsqueda
        txtBuscar.textProperty().addListener((obs, anterior, texto) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, anterior, estado) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((obs, anterior, categoria) -> aplicarFiltros());

        cargarCategorias();
        cargarProductos();
    }

    @FXML
    private void guardar() {
        Producto producto = leerFormulario(null);
        if (producto == null) {
            return;
        }

        try {
            productoDAO.guardar(producto);
            productos.add(producto);
            mostrarMensaje(Alert.AlertType.INFORMATION, "Producto guardado correctamente.");
            limpiar();
            aplicarFiltros();
        } catch (SQLException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        if (productoSeleccionado == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Seleccione un producto de la tabla.");
            return;
        }

        Producto datos = leerFormulario(productoSeleccionado.getId());
        if (datos == null) {
            return;
        }

        // Se modifica el mismo objeto seleccionado, no se crea un registro nuevo
        productoSeleccionado.setCodigo(datos.getCodigo());
        productoSeleccionado.setNombre(datos.getNombre());
        productoSeleccionado.setCategoria(datos.getCategoria());
        productoSeleccionado.setPrecioVenta(datos.getPrecioVenta());
        productoSeleccionado.setExistencia(datos.getExistencia());
        productoSeleccionado.setRutaImagen(datos.getRutaImagen());
        productoSeleccionado.setActivo(datos.isActivo());

        try {
            productoDAO.actualizar(productoSeleccionado);
            mostrarMensaje(Alert.AlertType.INFORMATION, "Producto actualizado correctamente.");
            limpiar();
        } catch (SQLException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo actualizar: " + e.getMessage());
            // Se recarga para que la tabla vuelva a mostrar lo que hay en la base de datos
            cargarProductos();
        }

        tablaProductos.refresh();
        aplicarFiltros();
    }

    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Seleccione un producto de la tabla.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el producto \"" + productoSeleccionado.getNombre() + "\"?");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            productoDAO.eliminar(productoSeleccionado.getId());
            productos.remove(productoSeleccionado);
            limpiar();
            actualizarResultados();
        } catch (SQLException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo eliminar: " + e.getMessage());
        }
    }

    @FXML
    private void limpiar() {
        productoSeleccionado = null;
        txtCodigo.clear();
        txtNombre.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        txtPrecio.clear();
        txtExistencia.clear();
        txtRutaImagen.clear();
        chkActivo.setSelected(true);
        tablaProductos.getSelectionModel().clearSelection();
    }

    @FXML
    private void buscar() {
        aplicarFiltros();
    }

    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        cmbFiltroEstado.setValue(ESTADO_TODOS);
        cmbFiltroCategoria.setValue(TODAS_LAS_CATEGORIAS);
        aplicarFiltros();
    }

    @FXML
    private void volver() throws IOException {
        SceneManager.switchTo("/ni/edu/uam/facturacion/fxml/menu-principal.fxml");
    }

    private void aplicarFiltros() {
        String busqueda = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String estado = cmbFiltroEstado.getValue();
        Categoria categoria = cmbFiltroCategoria.getValue();

        productosFiltrados.setPredicate(producto ->
                coincideBusqueda(producto, busqueda)
                        && coincideEstado(producto, estado)
                        && coincideCategoria(producto, categoria));

        actualizarResultados();
    }

    // La búsqueda no distingue mayúsculas de minúsculas: laptop, Laptop y LAPTOP dan lo mismo
    private boolean coincideBusqueda(Producto producto, String busqueda) {
        if (busqueda.isEmpty()) {
            return true;
        }

        return producto.getCodigo().toLowerCase().contains(busqueda)
                || producto.getNombre().toLowerCase().contains(busqueda)
                || producto.getCategoria().getNombre().toLowerCase().contains(busqueda);
    }

    private boolean coincideEstado(Producto producto, String estado) {
        if (ESTADO_ACTIVOS.equals(estado)) {
            return producto.isActivo();
        }
        if (ESTADO_INACTIVOS.equals(estado)) {
            return !producto.isActivo();
        }
        return true;
    }

    private boolean coincideCategoria(Producto producto, Categoria categoria) {
        if (categoria == null || categoria == TODAS_LAS_CATEGORIAS) {
            return true;
        }
        return producto.getCategoria().getId().equals(categoria.getId());
    }

    private void actualizarResultados() {
        lblResultados.setText("Mostrando " + productosFiltrados.size()
                + " de " + productos.size() + " productos");
    }

    private void seleccionar(Producto producto) {
        if (producto == null) {
            return;
        }

        productoSeleccionado = producto;
        txtCodigo.setText(producto.getCodigo());
        txtNombre.setText(producto.getNombre());
        txtPrecio.setText(producto.getPrecioVenta().toPlainString());
        txtExistencia.setText(String.valueOf(producto.getExistencia()));
        txtRutaImagen.setText(producto.getRutaImagen());
        chkActivo.setSelected(producto.isActivo());

        // Se busca por id porque el objeto del ComboBox es otra instancia
        cmbCategoria.getItems().stream()
                .filter(c -> c.getId().equals(producto.getCategoria().getId()))
                .findFirst()
                .ifPresentOrElse(
                        c -> cmbCategoria.getSelectionModel().select(c),
                        () -> cmbCategoria.getSelectionModel().clearSelection()
                );
    }

    /**
     * Valida el formulario y devuelve un Producto con sus datos, o null si algo es incorrecto.
     *
     * @param idExcluido id del producto que se está actualizando (para que su propio código
     *                   no cuente como duplicado); null cuando es un producto nuevo
     */
    private Producto leerFormulario(Integer idExcluido) {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        Categoria categoria = cmbCategoria.getValue();

        if (codigo.isEmpty()) {
            mostrarMensaje(Alert.AlertType.WARNING, "El código es obligatorio.");
            return null;
        }

        if (nombre.isEmpty()) {
            mostrarMensaje(Alert.AlertType.WARNING, "El nombre es obligatorio.");
            return null;
        }

        if (categoria == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Debe seleccionar una categoría.");
            return null;
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            mostrarMensaje(Alert.AlertType.WARNING, "El precio debe ser un número, por ejemplo 25.50");
            return null;
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarMensaje(Alert.AlertType.WARNING, "El precio debe ser mayor que cero.");
            return null;
        }

        int existencia;
        try {
            existencia = Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException e) {
            mostrarMensaje(Alert.AlertType.WARNING, "La existencia debe ser un número entero.");
            return null;
        }

        if (existencia < 0) {
            mostrarMensaje(Alert.AlertType.WARNING, "La existencia no puede ser negativa.");
            return null;
        }

        if (existeCodigo(codigo, idExcluido)) {
            mostrarMensaje(Alert.AlertType.WARNING, "Ya existe un producto con el código " + codigo + ".");
            return null;
        }

        String rutaImagen = txtRutaImagen.getText() == null ? "" : txtRutaImagen.getText().trim();

        return new Producto(
                null,
                codigo,
                nombre,
                categoria,
                precio,
                existencia,
                rutaImagen.isEmpty() ? null : rutaImagen,
                chkActivo.isSelected()
        );
    }

    // Se revisa la lista original (no la filtrada) para no dejar pasar duplicados ocultos
    private boolean existeCodigo(String codigo, Integer idExcluido) {
        return productos.stream()
                .anyMatch(p -> p.getCodigo().equalsIgnoreCase(codigo)
                        && !p.getId().equals(idExcluido));
    }

    private void cargarCategorias() {
        try {
            cmbCategoria.setItems(FXCollections.observableArrayList(categoriaDAO.listarActivas()));

            ObservableList<Categoria> opcionesFiltro = FXCollections.observableArrayList(TODAS_LAS_CATEGORIAS);
            opcionesFiltro.addAll(categoriaDAO.listar());
            cmbFiltroCategoria.setItems(opcionesFiltro);
            cmbFiltroCategoria.setValue(TODAS_LAS_CATEGORIAS);
        } catch (SQLException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudieron cargar las categorías: " + e.getMessage());
        }
    }

    private void cargarProductos() {
        try {
            productos.setAll(productoDAO.listar());
        } catch (SQLException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudieron cargar los productos: " + e.getMessage());
        }
        aplicarFiltros();
    }

    private void mostrarMensaje(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo, mensaje);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
