package lesson24.services;

import lesson24.db.DaoFactory;
import lesson24.db.Model;
import lesson24.db.components.RecipesDao;
import lesson24.db.components.RecipesToIngredientsDao;
import lesson24.db.components.TransactionRequest;
import lesson24.db.sсhema.Recipe;
import lesson24.db.sсhema.RecipesToIngredients;
import lesson24.exceptions.BusinessException;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static lesson24.errors.ValidateMessages.*;

@Repository
public class RecipeService {
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    private final Recipe recipe;
    private List<IngredientService> ingredients;

    public RecipeService(Recipe value) {
        this.recipe = value;
    }

    public RecipeService(String name, String description) {
        this.recipe = new Recipe(name, description);
    }

    /**
     * Метод для сохранения / изменения рецепта в базе данных
     * В случае наличия новых ингредиентов - они сохраняются в
     * связующей таблице recipes_to_ingredients.
     * Предполагается, что при удалении ингредиента из рецепта,
     * ингредиент удаляется из БД сразу.
     * Если в ходе выполнения возникнет ошибка, то данные не будут
     * сохранены до её устранения.
     */
    public void save() {
        validate();

        try (DaoFactory factory = new DaoFactory()) {
            factory.get(TransactionRequest.class).action(() -> {
                List<RecipesToIngredients> relations;
                Model recipesDao = factory.get(RecipesDao.class);
                RecipesToIngredientsDao relationsDao = factory.get(RecipesToIngredientsDao.class);

                if (recipe.getId() != null) {
                    recipesDao.update(recipe);
                    relations = getUpdateRelations(relationsDao);
                } else {
                    recipe.setId(recipesDao.create(recipe));
                    relations = getRelations();
                }
                relationsDao.create(relations);
            });
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    /**
     * Метод возвращает из списка ингнредиентов только новые,
     * ещё не добавленные в БД ингредиенты
     *
     * @param daoModel - объект для осуществления запроса на
     *                 существующие ингредиенты в БД
     * @return список новых ингредиентов
     */
    private List<RecipesToIngredients> getUpdateRelations(Model daoModel) {
        List<RecipesToIngredients> relations = getRelations();

        if (isNull(relations)) {
            return null;
        }

        List<?> relationsList = daoModel.getList("recipe_id", recipe.getId().toString());
        if (relationsList.isEmpty()) {
            return relations;
        }

        List<RecipesToIngredients> existList = relationsList.stream()
                .map(item -> (RecipesToIngredients) item)
                .collect(toList());
        relations.removeAll(existList);

        return relations;
    }

    /**
     * Проверяет наличие ингредиентов в сервисе и возвращает их список
     *
     * @return список ингредиентов рецепта
     */
    private List<RecipesToIngredients> getRelations() {
        if (isNull(ingredients) || ingredients.isEmpty()) {
            return null;
        }

        return ingredients.stream()
                .map(item -> new RecipesToIngredients(
                        recipe.getId(),
                        item.getIngredient().getId(),
                        item.getAmount(),
                        item.getUnit().getId()
                )).collect(toList());
    }

    /**
     * Проверка заполнености обязательных полей формы рецепта
     */
    private void validate() {
        if (!recipe.isValid()) {
            throw new BusinessException(RECIPE_NOT_VALID);
        }
        if (recipe.getName().length() > MAX_NAME_LENGTH) {
            throw new BusinessException(NAME_LENGTH_ERROR);
        }
        if (recipe.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException(DESCRIPTION_LENGTH_ERROR);
        }
    }

    /**
     * Удаление рецепта и связанных с ним ингредиентов из базы данных
     * Если в ходе выполнения возникнет ошибка - транзакция откатится
     * до исходных значений.
     */
    public void remove() {
        if (isNull(recipe.getId())) {
            throw new BusinessException(RECIPE_NOT_FOUND);
        }
        try (DaoFactory factory = new DaoFactory()) {
            factory.get(TransactionRequest.class).action(() -> {
                Model recipesDao = factory.get(RecipesDao.class);
                Model recipesToIngredientsDao = factory.get(RecipesToIngredientsDao.class);

                if (!recipesDao.remove(recipe.getId())) {
                    throw new BusinessException(RECIPE_REMOVE_ERROR);
                }
                if (!isNull(ingredients) && ingredients.isEmpty()) {
                    recipesToIngredientsDao.remove(recipe.getId());
                }
            });
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    public List<IngredientService> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientService> ingredients) {
        this.ingredients = ingredients;
    }

    public void setFields(String name, String description) {
        recipe.setName(name);
        recipe.setDescription(description);
    }

    /**
     * Получение списка рецептов из БД
     *
     * @return список рецептов, находящихся в БД
     */
    public static List<Recipe> getList() {
        try (DaoFactory factory = new DaoFactory()) {
            Model recipesDao = factory.get(RecipesDao.class);
            List<?> modelsList = recipesDao.getList();
            return modelsList.stream()
                    .map(item -> (Recipe) item)
                    .collect(toList());
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }
}
