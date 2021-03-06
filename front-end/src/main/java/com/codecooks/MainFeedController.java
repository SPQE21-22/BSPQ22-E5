package com.codecooks;

import com.codecooks.serialize.PopularRecipeFeedData;
import com.codecooks.serialize.RecentRecipeFeedData;
import com.codecooks.serialize.RecipeFeedData;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.ResourceBundle;

public class MainFeedController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @FXML private HBox hbPopRecipes;
    @FXML private AnchorPane apPopRecipes;
    @FXML private HBox hbRecentRecipes;
    @FXML private AnchorPane apRecentRecipes;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        int amtOfPopRecipes = 0;
        int amtOfRecentRecipes = 0;
        int recipeDisplayLimit = 10;
        int recipeWidth = 175; int separation = 10; int rightMargin = 20;

        // Popular recipes
        WebTarget popularTarget = ServerConnection.getInstance().getTarget("recipes/popular");
        Response popularResponse = popularTarget.request(MediaType.APPLICATION_JSON).get();

        if (popularResponse.getStatus() == Response.Status.OK.getStatusCode()) {

            List<PopularRecipeFeedData> feeds = popularResponse.readEntity(new GenericType<List<PopularRecipeFeedData>>() {});
            amtOfPopRecipes = feeds.size();

            if (amtOfPopRecipes > 0) {
                for (int i = 0; i < amtOfPopRecipes && i < recipeDisplayLimit; i++) {

                    RecipeFeedData feedData = feeds.get(i);
                    VBox vb = new FeedRecipeContainer(feedData, FeedRecipeContainer.Mode.POPULAR).getContent();
                    hbPopRecipes.getChildren().add(vb);

                }

                hbPopRecipes.setPrefWidth( amtOfPopRecipes*recipeWidth + amtOfPopRecipes*separation + rightMargin);
                apPopRecipes.setPrefWidth( hbPopRecipes.getPrefWidth() );
            }

            else {
                hbPopRecipes.getChildren().add( new Label(resourceBundle.getString("label.not.found")) );
            }
        }


        // Recent recipes
        WebTarget recentTarget = ServerConnection.getInstance().getTarget("recipes/recent");
        Response recentResponse = recentTarget.request(MediaType.APPLICATION_JSON).get();

        if (recentResponse.getStatus() == Response.Status.OK.getStatusCode()) {

            List<RecentRecipeFeedData> feeds = recentResponse.readEntity(new GenericType<List<RecentRecipeFeedData>>() {});
            amtOfRecentRecipes = feeds.size();

            if (amtOfRecentRecipes > 0) {
                for (int i = 0; i < amtOfRecentRecipes && i < recipeDisplayLimit; i++) {

                    RecipeFeedData feedData = feeds.get(i);
                    VBox vb = new FeedRecipeContainer(feedData, FeedRecipeContainer.Mode.RECENT).getContent();
                    hbRecentRecipes.getChildren().add(vb);

                }

                hbRecentRecipes.setPrefWidth( amtOfRecentRecipes*recipeWidth + amtOfRecentRecipes*separation + rightMargin);
                apRecentRecipes.setPrefWidth( hbRecentRecipes.getPrefWidth() );

            } else {
                hbRecentRecipes.getChildren().add( new Label(resourceBundle.getString("label.not.found")));
            }

        }

    }
}

class FeedRecipeContainer {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    enum Mode { POPULAR, RECENT }
    private RecipeFeedData feedData;
    private Mode mode;
    @FXML private VBox vb;
    @FXML private FontIcon favIcon;

    FeedRecipeContainer(RecipeFeedData feedData, Mode m) {
        this.feedData = feedData;

        // INLINE CSS
        vb = new VBox();
        vb.setStyle("-fx-border-style: solid; -fx-border-color: grey; -fx-background-color: #eaeaea");
        vb.setPadding(new Insets(5,5,5,5));
        vb.setSpacing(5); vb.setPrefWidth(175); vb.setPrefHeight(85);

        // LABELS
        Label lTitle = new Label(feedData.getTitle());
        lTitle.setStyle("-fx-font-size: 16");
        Label lAuthor = new Label(feedData.getAuthor());

        // BUTTON MENU (BUTTONS AND ICONS)
        HBox buttonBox = new HBox();
        Button bOpen= new Button();

        //icons
        FontIcon viewIcon = new FontIcon(); viewIcon.setIconLiteral("ci-view"); viewIcon.setIconSize(16);
        bOpen.setGraphic(viewIcon);

        // misc
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setSpacing(5);

        // Type dependant
        if (m == Mode.POPULAR) {
            favIcon = new FontIcon();
            favIcon.setIconSize(16);
            favIcon.setIconLiteral("ci-star");
            int numFavs = ((PopularRecipeFeedData)feedData).getNumFavourites();
            Label lFavs = new Label(String.valueOf(numFavs));
            Label favContainer = new Label();
            favContainer.setGraphic(favIcon);
            buttonBox.getChildren().addAll(bOpen, favContainer, lFavs);

        } else if (m == Mode.RECENT) { // Mode.RECENT
            Label lDateTime = new Label();
            LocalDateTime recipeDateTime = ((RecentRecipeFeedData) feedData).getDateTime();

            if (recipeDateTime != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT);
                lDateTime.setText(recipeDateTime.format(formatter));

            } else {
                logger.warn("Failed to acquire date"
                        + "\n |_ recipeDateTime: " + recipeDateTime);
                lDateTime.setText("");
            }

            buttonBox.getChildren().addAll(bOpen, lDateTime);

        } else {
            buttonBox.getChildren().addAll(bOpen);
        }

        // BUTTON LISTENERS
        bOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                try {
                    RecipeShowingController controller = new RecipeShowingController();
                    controller.setRecipeId(feedData.getId());
                    controller.setFromView(RecipeShowingController.FromView.HOME);
                    App.setRoot("recipeShow", controller);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // FINAL STEP
        vb.getChildren().addAll(
                lTitle, lAuthor, buttonBox
        );

    }

    public VBox getContent() { return this.vb; }
}