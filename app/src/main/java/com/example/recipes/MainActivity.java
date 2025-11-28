package com.example.recipes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.recipes.adapters.RecipeAdapter;
import com.example.recipes.api.RetrofitClient;
import com.example.recipes.api.ApiService;
import com.example.recipes.models.Recipe;
import com.example.recipes.models.RecipeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recycler;
    private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Настройка RecyclerView
        recycler = findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecipeAdapter(this);
        recycler.setAdapter(adapter);

        // Загрузка данных
        loadRecipes();
    }

    private void loadRecipes() {
        ApiService api = RetrofitClient.getApi();

        api.getRecipes().enqueue(new Callback<RecipeResponse>() {
            @Override
            public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Recipe> recipes = response.body().getRecipes();

                    if (recipes != null && !recipes.isEmpty()) {
                        Log.d(TAG, "Загружено рецептов: " + recipes.size());
                        adapter.setData(recipes);
                    } else {
                        Toast.makeText(MainActivity.this, "Нет рецептов", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RecipeResponse> call, Throwable t) {
                Log.e(TAG, "Ошибка: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}