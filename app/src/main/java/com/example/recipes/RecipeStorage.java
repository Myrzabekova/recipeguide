package com.example.recipes.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.recipes.models.Recipe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RecipeStorage {

    private static final String PREF_NAME = "RecipePreferences";
    private static final String KEY_RECIPES = "local_recipes";
    private static final String KEY_NEXT_ID = "next_recipe_id";

    private SharedPreferences prefs;
    private Gson gson;

    public RecipeStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Получить все локально сохраненные рецепты
     */
    public List<Recipe> getLocalRecipes() {
        String json = prefs.getString(KEY_RECIPES, "[]");
        Type type = new TypeToken<List<Recipe>>(){}.getType();
        List<Recipe> recipes = gson.fromJson(json, type);
        return recipes != null ? recipes : new ArrayList<>();
    }

    /**
     * Сохранить список рецептов
     */
    private void saveRecipes(List<Recipe> recipes) {
        String json = gson.toJson(recipes);
        prefs.edit().putString(KEY_RECIPES, json).apply();
    }

    /**
     * Добавить новый рецепт
     */
    public boolean addRecipe(Recipe recipe) {
        try {
            List<Recipe> recipes = getLocalRecipes();
            recipes.add(recipe);
            saveRecipes(recipes);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Удалить рецепт по ID
     */
    public boolean deleteRecipe(int recipeId) {
        try {
            List<Recipe> recipes = getLocalRecipes();
            Recipe toRemove = null;
            for (Recipe recipe : recipes) {
                if (recipe.getId() == recipeId) {
                    toRemove = recipe;
                    break;
                }
            }
            if (toRemove != null) {
                recipes.remove(toRemove);
                saveRecipes(recipes);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Получить следующий ID для нового рецепта
     */
    private int getNextId() {
        int nextId = prefs.getInt(KEY_NEXT_ID, 1000);
        prefs.edit().putInt(KEY_NEXT_ID, nextId + 1).apply();
        return nextId;
    }

    /**
     * Создать новый рецепт с автоматическим ID
     */
    public Recipe createRecipe(String name, String category, int cookingTime,
                               String difficulty, List<String> ingredients,
                               String description, String imageUrl) {
        Recipe recipe = new Recipe();
        recipe.setId(getNextId());
        recipe.setName(name);
        recipe.setCategory(category);
        recipe.setCookingTime(cookingTime);
        recipe.setDifficulty(difficulty);
        recipe.setIngredients(ingredients);
        recipe.setDescription(description);
        recipe.setImageUrl(imageUrl);

        return recipe;
    }

    /**
     * Проверка избранного
     */
    public boolean isFavorite(int recipeId) {
        String key = "favorite_" + recipeId;
        return prefs.getBoolean(key, false);
    }

    /**
     * Добавить/удалить из избранного
     */
    public void toggleFavorite(int recipeId) {
        String key = "favorite_" + recipeId;
        boolean current = prefs.getBoolean(key, false);
        prefs.edit().putBoolean(key, !current).apply();
    }

    /**
     * Получить все избранные рецепты
     */
    public List<Integer> getFavoriteIds() {
        List<Integer> favoriteIds = new ArrayList<>();
        // Проверяем все возможные ID (от API и локальные)
        for (int i = 1; i < 1000; i++) {
            if (isFavorite(i)) {
                favoriteIds.add(i);
            }
        }
        for (int i = 1000; i < getNextId(); i++) {
            if (isFavorite(i)) {
                favoriteIds.add(i);
            }
        }
        return favoriteIds;
    }
}