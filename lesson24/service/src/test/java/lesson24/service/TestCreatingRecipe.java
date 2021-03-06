package lesson24.service;

import lesson24.db.DaoFactory;
import lesson24.db.Model;
import lesson24.db.components.IngredientsDao;
import lesson24.db.components.RecipesDao;
import lesson24.db.components.RecipesToIngredientsDao;
import lesson24.db.sсhema.Recipe;
import lesson24.db.sсhema.Unit;
import lesson24.exceptions.BusinessException;
import lesson24.services.IngredientService;
import lesson24.services.RecipeService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestCreatingRecipe {
    @Mock
    public List<IngredientService> mockedIngredientServiceList;

    @Test
    @Ignore
    public void transactions() throws Exception {
        when(mockedIngredientServiceList.size()).thenThrow(new BusinessException("Test!"));
        try {
            RecipeService recipeService = new RecipeService("test", "descr");
            recipeService.setIngredients(mockedIngredientServiceList);
            recipeService.save();
            fail();
        } catch (BusinessException e) {
            try (DaoFactory factory = new DaoFactory()) {
                Model recipesDao = factory.get(RecipesDao.class);
                List<?> listOptional = recipesDao.getList("name", "test");
                assertTrue(listOptional.isEmpty());
            }
        }
    }

    @Test(expected = BusinessException.class)
    public void wrongName() {
        RecipeService recipeService = new RecipeService("", "descr");
        recipeService.save();
    }

    @Test(expected = BusinessException.class)
    public void wrongDescription() {
        RecipeService recipeService = new RecipeService("test", "");
        recipeService.save();
    }

    @Test
    @Ignore
    public void createAndDelete() {
        RecipeService recipeService = new RecipeService("test", "descr");
        Unit unit = new Unit(1L, "test1", "ttt");
        List<IngredientService> ingredients = Arrays.asList(
                new IngredientService("test1", unit, "10"),
                new IngredientService("test2", unit, "15"),
                new IngredientService("test3", unit, "20")
        );
        ingredients.forEach(IngredientService::save);
        recipeService.setIngredients(ingredients);
        recipeService.save();

        try (DaoFactory factory = new DaoFactory()) {
            IngredientsDao ingredientsDao = factory.get(IngredientsDao.class);
            List<?> ingList = ingredientsDao.getList("id", ingredients.get(0).getIngredient().getId().toString());
            assertTrue(!ingList.isEmpty());

            Model recipesDao = factory.get(RecipesDao.class);
            List<?> listOptional = recipesDao.getList("name", "test");
            assertTrue(!listOptional.isEmpty());
            Recipe recipe = (Recipe) listOptional.get(0);

            RecipesToIngredientsDao recipesToIngredients = factory.get(RecipesToIngredientsDao.class);
            List<?> rtiList = recipesToIngredients.getList("recipe_id", recipe.getId().toString());
            assertTrue(!rtiList.isEmpty());

            //Удаление
            RecipeService recipeService1 = new RecipeService(recipe);
            recipeService1.remove();

            List<?> listOptional1 = recipesDao.getList("name", "test");
            assertTrue(listOptional1.isEmpty());

            List<?> rtiList1 = recipesToIngredients.getList("recipe_id", recipe.getId().toString());
            assertTrue(rtiList1.isEmpty());

            ingredients.forEach(IngredientService::remove);
            List<?> ingList1 = ingredientsDao.getList("id", ingredients.get(0).getIngredient().getId().toString());
            assertTrue(ingList1.isEmpty());
        }
    }
}
