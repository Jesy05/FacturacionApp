package ni.edu.uam.facturacion.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.facturacion.util.SceneManager;

import java.io.IOException;

public class FacturacionApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setPrimaryStage(stage);

        FXMLLoader fxmlLoader = new FXMLLoader(
                FacturacionApplication.class.getResource("/ni/edu/uam/facturacion/fxml/menu-principal.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        stage.setTitle("Sistema de Facturación");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
