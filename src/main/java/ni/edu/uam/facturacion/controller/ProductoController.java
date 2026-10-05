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

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

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

        SortedList<Producto> productosOrdenados = new SortedList<>(productosFiltrados);
        productosOrdenados.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(productosOrdenados);

        tablaProductos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nuevo) -> seleccionar(nuevo));

        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                ESTADO_TODOS, ESTADO_ACTIVOS, ESTADO_INACTIVOS));
        cmbFiltroEstado.setValue(ESTADO_TODOS);

        txtBuscar.textProperty().addListener((obs, anterior, texto) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, anterior, estado) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((obs, anterior, categoria) -> aplicarFiltros());

        cargarCategorias();
        cargarProductos();
    }

    @FXML
    private void guardar() {
        try {
            Producto producto = obtenerProductoFormulario();

            if (productoDAO.existeCodigo(producto.getCodigo())) {
                Mensajes.mostrarAdvertencia("Código duplicado", "Ya existe un producto con ese código.");
                txtCodigo.requestFocus();
                return;
            }

            productoDAO.guardar(producto);
            productos.add(producto);
            Mensajes.mostrarExito("Producto registrado", "La información fue almacenada correctamente.");
            limpiar();
            aplicarFiltros();
        } catch (IllegalArgumentException e) {
            Mensajes.mostrarAdvertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            Mensajes.mostrarErrorBaseDatos("No fue posible registrar el producto.", e);
        }
    }

    @FXML
    private void actualizar() {
        if (productoSeleccionado == null) {
            Mensajes.mostrarAdvertencia("Seleccione un producto",
                    "Debe seleccionar el producto que desea actualizar.");
            return;
        }

        try {
            // Se validan nuevamente todos los campos antes del UPDATE
            Producto datos = obtenerProductoFormulario();

            // El código no puede pertenecer a otro producto
            if (productoDAO.existeCodigo(datos.getCodigo(), productoSeleccionado.getId())) {
                Mensajes.mostrarAdvertencia("Código duplicado", "Ya existe un producto con ese código.");
                txtCodigo.requestFocus();
                return;
            }

            // Se modifica el mismo objeto seleccionado, no se crea un registro nuevo
            productoSeleccionado.setCodigo(datos.getCodigo());
            productoSeleccionado.setNombre(datos.getNombre());
            productoSeleccionado.setCategoria(datos.getCategoria());
            productoSeleccionado.setPrecioVenta(datos.getPrecioVenta());
            productoSeleccionado.setExistencia(datos.getExistencia());
            productoSeleccionado.setActivo(datos.isActivo());

            productoDAO.actualizar(productoSeleccionado);
            Mensajes.mostrarExito("Producto actualizado", "El producto se actualizó correctamente.");
            limpiar();
        } catch (IllegalArgumentException e) {
            Mensajes.mostrarAdvertencia("Validación", e.getMessage());
            return;
        } catch (SQLException e) {
            Mensajes.mostrarErrorBaseDatos("No fue posible actualizar el producto.", e);
            // se recarga para que la tabla vuelva a mostrar lo que hay en la base de datos
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
            Mensajes.mostrarErrorBaseDatos("No fue posible eliminar el producto.", e);
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


    private Producto obtenerProductoFormulario() {
        String codigo = texto(txtCodigo);
        String nombre = texto(txtNombre);

        if (codigo.isEmpty()) {
            txtCodigo.requestFocus();
            throw new IllegalArgumentException("El código es obligatorio.");
        }

        if (nombre.isEmpty()) {
            txtNombre.requestFocus();
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        Categoria categoria = cmbCategoria.getSelectionModel().getSelectedItem();

        if (categoria == null) {
            cmbCategoria.requestFocus();
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }

        // Lo que viene de un TextField es texto: si no es un número, la conversión lanza NumberFormatException
        BigDecimal precio;
        try {
            precio = new BigDecimal(texto(txtPrecio));
        } catch (NumberFormatException e) {
            txtPrecio.requestFocus();
            throw new IllegalArgumentException("El precio debe ser un valor numérico.");
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            txtPrecio.requestFocus();
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero.");
        }

        // parseInt también rechaza decimales como 10.5
        int existencia;
        try {
            existencia = Integer.parseInt(texto(txtExistencia));
        } catch (NumberFormatException e) {
            txtExistencia.requestFocus();
            throw new IllegalArgumentException("La existencia debe ser un número entero.");
        }

        if (existencia < 0) {
            txtExistencia.requestFocus();
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
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

    private String texto(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private void cargarCategorias() {
        try {
            cmbCategoria.setItems(FXCollections.observableArrayList(categoriaDAO.listarActivas()));

            ObservableList<Categoria> opcionesFiltro = FXCollections.observableArrayList(TODAS_LAS_CATEGORIAS);
            opcionesFiltro.addAll(categoriaDAO.listar());
            cmbFiltroCategoria.setItems(opcionesFiltro);
            cmbFiltroCategoria.setValue(TODAS_LAS_CATEGORIAS);
        } catch (SQLException e) {
            Mensajes.mostrarErrorBaseDatos("No fue posible cargar las categorías. Verifique la conexión con la base de datos.", e);
        }
    }

    private void cargarProductos() {
        try {
            productos.setAll(productoDAO.listar());
        } catch (SQLException e) {
            Mensajes.mostrarErrorBaseDatos("No fue posible cargar los productos. Verifique la conexión con la base de datos.", e);
        }
        aplicarFiltros();
    }
}
