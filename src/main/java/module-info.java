module ni.edu.uam.facturacion {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;


    opens ni.edu.uam.facturacion.application to javafx.fxml, javafx.graphics;
    opens ni.edu.uam.facturacion.controller to javafx.fxml;

    exports ni.edu.uam.facturacion.application;
}
