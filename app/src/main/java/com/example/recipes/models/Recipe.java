package com.example.recipes.models;

import java.io.Serializable;
import java.util.List;

public class Recipe implements Serializable {

    private int id;
    private String name;
    private String category;
    private int cookingTime;
    private String difficulty;
    private List<String> ingredients;
    private String description;
    private String imageUrl;

    // Конструктор
    public Recipe() {}

    // Геттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getCookingTime() { return cookingTime; }
    public String getDifficulty() { return difficulty; }
    public List<String> getIngredients() { return ingredients; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Вспомогательные методы
    public String getCookingTimeString() {
        return cookingTime + " мин.";
    }

    public int getIngredientsCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    public String getIngredientsCountString() {
        return getIngredientsCount() + " ингр.";
    }
}