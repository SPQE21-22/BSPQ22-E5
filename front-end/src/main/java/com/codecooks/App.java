package com.codecooks;

import com.codecooks.utils.I18n;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Parent previousFXML;

    @Override
    public void start(Stage stage) throws IOException {

        double height = 400.0;
        double width  = 600.0;
        scene = new Scene(loadFXML("login"), width, height);
        stage.setScene(scene);
        stage.setTitle("Recipe Master");
        stage.getIcons().add(new Image(Objects.requireNonNull(App.class.getResourceAsStream("img/food-icon-cc-64.png"))));
        stage.sizeToScene();
        stage.show();
        stage.setMinWidth(width);
        stage.setMinHeight(height);
    }

    static void setRoot(String fxml) throws IOException {
        previousFXML = scene.getRoot();
        scene.setRoot(loadFXML(fxml));
    }

    static void setRoot(String fxml, Object controller) throws IOException {
        previousFXML = scene.getRoot();
        scene.setRoot(loadFXML(fxml, controller));
    }

    static Optional<ButtonType> showAlertAndWait(Alert alert) {

        alert.initOwner(scene.getWindow());
        return alert.showAndWait();
    }

    static void goBack() throws IOException {
        scene.setRoot(previousFXML);
    }

    protected static Parent loadFXML(String fxml) throws IOException {

        return FXMLLoader.load(
                Objects.requireNonNull(App.class.getResource(fxml + ".fxml")),
                I18n.getFXMLBundle(fxml)
        );
    }

    protected static Parent loadFXML(String fxml, Object controller) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        fxmlLoader.setResources(I18n.getFXMLBundle(fxml));
        fxmlLoader.setController(controller);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {

        AppConfiguration.loadConfig();
        launch();
    }

}