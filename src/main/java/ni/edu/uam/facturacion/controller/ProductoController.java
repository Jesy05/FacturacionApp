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
import ni.edu.uam.facturacion.util.Mensajes;
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
            Mensajes.mostrarExito("Producto registrado", "El producto se guardó correctamente.");
            limpiar();
            aplicarFiltros();
        } catch (SQLException e) {
            Mensajes.mostrarError("Error de base de datos", "No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        if (productoSeleccionado == null) {
            Mensajes.mostrarAdvertencia("Seleccione un producto", "Seleccione un producto de la tabla.");
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
        productoSeleccionado.setActivo(datos.isActivo());

        try {
            productoDAO.actualizar(productoSeleccionado);
            Mensajes.mostrarExito("Producto actualizado", "El producto se actualizó correctamente.");
            limpiar();
        } catch (SQLException e) {
            Mensajes.mostrarError("Error de base de datos", "No se pudo actualizar: " + e.getMessage());
            // Se recarga para que la tabla vuelva a mostrar lo que hay en la base de datos
            cargarProductos();
        }

        tablaProductos.refresh();
        aplicarFiltros();
    }

    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            Mensajes.mostrarAdvertencia("Seleccione un producto", "Seleccione un producto de la tabla.");
            return;
        }

        if (!Mensajes.confirmar("Eliminar producto",
                "¿Eliminar el producto \"" + productoSeleccionado.getNombre() + "\"?")) {
            return;
        }

        try {
            productoDAO.eliminar(productoSeleccionado.getId());
            productos.remove(productoSeleccionado);
            limpiar();
            actualizarResultados();
        } catch (SQLException e) {
            Mensajes.mostrarError("Error de base de datos", "No se pudo eliminar: " + e.getMessage());
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
            Mensajes.mostrarAdvertencia("Validación", "El código es obligatorio.");
            return null;
        }

        if (nombre.isEmpty()) {
            Mensajes.mostrarAdvertencia("Validación", "El nombre es obligatorio.");
            return null;
        }

        if (categoria == null) {
            Mensajes.mostrarAdvertencia("Validación", "Debe seleccionar una categoría.");
            return null;
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            Mensajes.mostrarAdvertencia("Precio incorrecto", "El precio debe ser un valor numérico.");
            return null;
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            Mensajes.mostrarAdvertencia("Precio incorrecto", "El precio de venta debe ser mayor que cero.");
            return null;
        }

        int existencia;
        try {
            existencia = Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException e) {
            Mensajes.mostrarAdvertencia("Existencia incorrecta", "La existencia debe ser un número entero.");
            return null;
        }

        if (existencia < 0) {
            Mensajes.mostrarAdvertencia("Existencia incorrecta", "La existencia no puede ser negativa.");
            return null;
        }

        if (existeCodigo(codigo, idExcluido)) {
            Mensajes.mostrarAdvertencia("Código duplicado", "Ya existe un producto con ese código.");
            return null;
        }

        return new Producto(
                null,
                codigo,
                nombre,
                categoria,
                precio,
                existencia,
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
            Mensajes.mostrarError("Error de base de datos", "No se pudieron cargar las categorías: " + e.getMessage());
        }
    }

    private void cargarProductos() {
        try {
            productos.setAll(productoDAO.listar());
        } catch (SQLException e) {
            Mensajes.mostrarError("Error de base de datos", "No se pudieron cargar los productos: " + e.getMessage());
        }
        aplicarFiltros();
    }
}
