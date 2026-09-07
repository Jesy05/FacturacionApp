package ni.edu.uam.facturacion.controller;

import javafx.fxml.FXML;
import ni.edu.uam.facturacion.util.SceneManager;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    private void initialize() {
    }

    @FXML
    private void abrirProductos() throws IOException {
        SceneManager.switchTo("/ni/edu/uam/facturacion/fxml/producto-view.fxml");
    }
}
