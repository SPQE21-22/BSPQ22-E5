package com.codecooks;

import com.codecooks.domain.CookingExperience;
import com.codecooks.domain.Gender;
import com.codecooks.serialize.ProfileEditionData;
import com.codecooks.utils.CountryManager;
import com.codecooks.utils.I18n;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ProfileEditionController implements Initializable {

    private ObservableList<CookingExperience> cookingExpOptions =
            FXCollections.observableArrayList(CookingExperience.values());
    private ObservableList<Gender> genderOptions =
            FXCollections.observableArrayList(Gender.values());
    private ObservableList<String> countryOptions =
            FXCollections.observableArrayList();

    @FXML private ComboBox<CookingExperience> cbCookingExp;
    @FXML private ComboBox<Gender> cbGender;
    @FXML private ComboBox<String> cbCountry;
    @FXML private DatePicker dpBirthDate;
    @FXML private ImageView ivUserAvatar;
    @FXML private Label lUsername;
    @FXML private TextField tfName;
    @FXML private TextArea taAboutMe;

    private WebTarget target;
    private CountryManager countryManager;

    public ProfileEditionController() {

        countryManager = new CountryManager();

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        target = ServerConnection.getInstance().getTarget("users/me/edit");

        // When setting the converter, remember to check for null values (in case the user did not set the value yet)
        cbCookingExp.setItems(cookingExpOptions);
        cbCookingExp.setConverter(new StringConverter<CookingExperience>() {
            @Override
            public String toString(CookingExperience cookingExperience) {
                if (cookingExperience == null) return "";
                else return I18n.getEnumTranslation(cookingExperience);
            }

            @Override
            public CookingExperience fromString(String s) {
                return null;
            }
        });

        cbGender.setItems(genderOptions);
        cbGender.setConverter(new StringConverter<Gender>() {
            @Override
            public String toString(Gender gender) {
                if (gender == null) return "";
                else return I18n.getEnumTranslation(gender);
            }

            @Override
            public Gender fromString(String s) {
                return null;
            }
        });

        // Add countries to combo box
        cbCountry.getItems().setAll(countryManager.getCountryNames());
        cbCountry.setVisibleRowCount(8);


        Response response = target.request(MediaType.APPLICATION_JSON).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {

            ProfileEditionData data = response.readEntity(ProfileEditionData.class);

            lUsername.setText(data.getUsername());
            if (data.getName() != null) tfName.setText(data.getName());
            if (data.getBirthDate() != null) dpBirthDate.setValue(data.getBirthDate());
            if (data.getCountryCode() != null) cbCountry.setValue(countryManager.getNameFromCode(data.getCountryCode()));
            if (data.getGender() != null) cbGender.setValue(data.getGender());
            if (data.getCookingExp() != null) cbCookingExp.setValue(data.getCookingExp());
            if (data.getAboutMe() != null) taAboutMe.setText(data.getAboutMe());

        }
    }

    @FXML
    private void changePicture() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg")
        );

        File selectedFile = fileChooser.showOpenDialog(ivUserAvatar.getScene().getWindow());
        if (selectedFile != null) {

            ivUserAvatar.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void goBack() throws IOException {
            App.goBack();
    }

    @FXML
    private void saveChanges() {

        String name = tfName.getText();
        LocalDate birthDate = dpBirthDate.getValue();
        String country = cbCountry.getValue();
        Gender gender = cbGender.getValue();
        CookingExperience cookingExp = cbCookingExp.getValue();

        ProfileEditionData data = new ProfileEditionData();
        data.setName(name);
        data.setBirthDate(birthDate);
        data.setCountryCode(countryManager.getCodeFromName(country));
        data.setGender(gender);
        data.setCookingExp(cookingExp);
        data.setAboutMe(taAboutMe.getText());

        Response response = target.request().put(Entity.entity(data, MediaType.APPLICATION_JSON));
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {

            try {
                MainController controller = new MainController();
                controller.setSubView(MainController.SubView.PROFILE);
                App.setRoot("main", controller);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
