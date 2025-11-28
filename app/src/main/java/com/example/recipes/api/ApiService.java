package com.example.recipes.api;

import com.example.recipes.models.RecipeResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("recipes")
    Call<RecipeResponse> getRecipes();


}