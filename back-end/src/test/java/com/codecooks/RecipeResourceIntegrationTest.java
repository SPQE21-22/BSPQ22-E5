package com.codecooks;

import com.codecooks.authentication.SessionManager;
import com.codecooks.dao.RecipeDAO;
import com.codecooks.dao.UserDAO;
import com.codecooks.domain.Recipe;
import com.codecooks.domain.User;
import com.codecooks.serialize.RecipeData;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RecipeResourceIntegrationTest {

    private static HttpServer server;
    private static WebTarget target;
    private static String token;

    private static final UserDAO userDAO = new UserDAO();
    private static User user;

    private static final RecipeDAO recipeDAO = new RecipeDAO();
    List<Recipe> recipes = new ArrayList<>();

    @BeforeClass
    public static void setUp() {

        server = Main.startServer();
        Client c = ClientBuilder.newClient();
        target = c.target(Main.BASE_URI);

        token = SessionManager.getInstance().generateToken();
        SessionManager.getInstance().startSession(token, "test");

    }

    @Before
    public void prepareRecipes() {

        user = new User("test", "test@gmail.ocm", "5678");
        userDAO.save(user);

        Recipe recipe1 = new Recipe("Test recipe 1", "Test content 1", "ES");
        recipe1.setCreator(user);

        Recipe recipe2 = new Recipe("Test recipe 2", "Test content 2", "UK");
        recipe2.setCreator(user);

        recipeDAO.save(recipe1);
        recipeDAO.save(recipe2);

        recipes.add(recipe1);
        recipes.add(recipe2);
    }

    @Test
    public void testPostRecipe() {

        WebTarget postTarget = target.path("recipes");

        RecipeData recipeData = new RecipeData();
        recipeData.setTitle("Post test");
        recipeData.setContent("Posting new recipe!");
        recipeData.setCountryCode("UK");

        Response response = postTarget.request()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .post(Entity.entity(recipeData, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        response.close();

        List<Recipe> dbRecipes = recipeDAO.findAll();
        assertEquals(recipes.size() + 1, dbRecipes.size());

    }

    @Test
    public void testGetRecipe() {

        Recipe recipe = recipes.get(0);
        WebTarget getTarget = target.path("recipes/" + recipe.getId());

        Response response = getTarget.request(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RecipeData recipeData = response.readEntity(RecipeData.class);

        assertEquals(recipe.getTitle(), recipeData.getTitle());
        assertEquals(recipe.getContent(), recipeData.getContent());
        assertEquals(recipe.getCountryCode(), recipeData.getCountryCode());

    }

    @Test
    public void testEditRecipe() {

        Recipe recipe = recipes.get(0);
        WebTarget editTarget = target.path("recipes/" + recipe.getId());

        String modTitle = "Modified title";
        String modContent = "Modified title";
        String modCountryCode = "AR";

        RecipeData recipeData = new RecipeData();
        recipeData.setTitle(modTitle);
        recipeData.setContent(modContent);
        recipeData.setCountryCode(modCountryCode);

        Response response = editTarget.request()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .put(Entity.entity(recipeData, MediaType.APPLICATION_JSON));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        response.close();

        Recipe dbRecipe = recipeDAO.findBy("id", recipe.getId());
        assertEquals(modTitle, dbRecipe.getTitle());
        assertEquals(modContent, dbRecipe.getContent());
        assertNotEquals(modCountryCode, dbRecipe.getCountryCode()); //Not allowed

    }

    @Test
    public void deleteRecipe() {

        Recipe recipe = recipes.get(0);
        WebTarget delTarget = target.path("recipes/" + recipe.getId());

        Response response = delTarget.request()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .delete();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        response.close();

        List<Recipe> dbRecipes = recipeDAO.findAll();
        assertEquals(recipes.size() - 1, dbRecipes.size());
        assertFalse(dbRecipes.contains(recipe));

    }

    @Test
    public void addFavouriteRecipe() {

        Recipe recipe = recipes.get(0);
        WebTarget favTarget = target.path("recipes/" + recipe.getId() + "/favourite");

        Response response = favTarget.request()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        User dbUser = userDAO.findBy("username", user.getUsername());
        assertEquals(1, dbUser.getFavouriteRecipes().size());
        assertTrue(dbUser.getFavouriteRecipes().contains(recipe));

    }

    @Test
    public void deleteFavouriteRecipe() {

        Recipe recipe = recipes.get(0);
        recipe.addUserLinkedToFav(user);
        recipeDAO.save(recipe);

        WebTarget favTarget = target.path("recipes/" + recipe.getId() + "/favourite");

        Response response = favTarget.request()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .delete();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        response.close();

        User dbUser = userDAO.findBy("username", user.getUsername());
        assertEquals(0, dbUser.getFavouriteRecipes().size());
    }

    @After
    public void removeRecipes() {

        userDAO.deleteAll();
        recipeDAO.deleteAll();

    }


    @AfterClass
    public static void tearDown() {

        SessionManager.getInstance().endSession("test");
        server.shutdown();
    }
}
