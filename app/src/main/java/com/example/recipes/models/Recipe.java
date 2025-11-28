package com.example.recipes.models;

import java.util.List;

public class Recipe {

    private int id;
    private String name;
    private String category;
    private int cookingTime;
    private String difficulty;
    private List<String> ingredients;
    private String description;
    private String imageUrl;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getCookingTime() { return cookingTime; }
    public String getDifficulty() { return difficulty; }
    public List<String> getIngredients() { return ingredients; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
}
