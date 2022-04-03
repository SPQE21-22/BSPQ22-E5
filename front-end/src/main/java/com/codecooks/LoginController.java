package com.codecooks;

import com.codecooks.serialize.Credentials;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML private TextField tfEmail;
    @FXML private PasswordField passField;


    @FXML
    private void login() {

        Credentials credentials = new Credentials();
        credentials.setEmail(tfEmail.getText());
        credentials.setPassword(org.apache.commons.codec.digest.DigestUtils.sha1Hex(passField.getText()));

        WebTarget target = ServerConnection.getInstance().getTarget("/account/login");
        Response response = target.request(MediaType.TEXT_PLAIN).post(Entity.entity(credentials, MediaType.APPLICATION_JSON));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {

            String token = response.readEntity(String.class);
            ServerConnection.getInstance().setAuthToken(token);
            System.out.println("Token: " + token);

        }

        else {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Invalid credentials!");
            alert.showAndWait();
        }
    }

    @FXML
    private void toRegister() {

        try {
            App.setRoot("register");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
