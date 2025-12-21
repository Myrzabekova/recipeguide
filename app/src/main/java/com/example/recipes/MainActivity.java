package com.example.recipes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipes.adapters.RecipeAdapter;
import com.example.recipes.api.RetrofitClient;
import com.example.recipes.api.ApiService;
import com.example.recipes.models.Recipe;
import com.example.recipes.models.RecipeResponse;
import com.example.recipes.utils.RecipeStorage;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ADD_RECIPE = 1;
    private static final int REQUEST_VIEW_RECIPE = 2;

    private RecyclerView recycler;
    private RecipeAdapter adapter;
    private EditText searchEditText;
    private ChipGroup categoryChipGroup;
    private TextView recipeCountText;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddRecipe;

    private List<Recipe> allRecipes;
    private String currentCategory = "Все";
    private RecipeStorage recipeStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recipeStorage = new RecipeStorage(this);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupCategoryFilter();
        setupFab();

        loadRecipes();
    }

    private void initViews() {
        recycler = findViewById(R.id.recyclerRecipes);
        searchEditText = findViewById(R.id.searchEditText);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        recipeCountText = findViewById(R.id.recipeCountText);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
        fabAddRecipe = findViewById(R.id.fabAddRecipe);
    }

    private void setupRecyclerView() {
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, recipeStorage);

        adapter.setOnRecipeClickListener(recipe -> {
            Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe", recipe);
            startActivityForResult(intent, REQUEST_VIEW_RECIPE);
        });

        recycler.setAdapter(adapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes(s.toString(), currentCategory);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryFilter() {
        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                currentCategory = chip.getText().toString();
                filterRecipes(searchEditText.getText().toString(), currentCategory);
            }
        });
    }

    private void setupFab() {
        fabAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddRecipeActivity.class);
            startActivityForResult(intent, REQUEST_ADD_RECIPE);
        });
    }

    private void filterRecipes(String query, String category) {
        List<Recipe> filteredList = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            boolean matchesQuery = recipe.getName().toLowerCase()
                    .contains(query.toLowerCase());

            boolean matchesCategory;
            if (category.equals("Избранное")) {
                // Фильтр по избранным
                matchesCategory = recipeStorage.isFavorite(recipe.getId());
            } else {
                // Обычный фильтр по категории
                matchesCategory = category.equals("Все") ||
                        recipe.getCategory().equals(category);
            }

            if (matchesQuery && matchesCategory) {
                filteredList.add(recipe);
            }
        }

        adapter.filter(query, category);
        updateRecipeCount();
        updateEmptyState();
    }

    private void updateRecipeCount() {
        int count = adapter.getItemCount();
        if (currentCategory.equals("Избранное")) {
            recipeCountText.setText("⭐ Избранных рецептов: " + count);
        } else {
            recipeCountText.setText("Найдено рецептов: " + count);
        }
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recycler.setVisibility(View.VISIBLE);
        }
    }

    private void loadRecipes() {
        showLoading(true);

        ApiService api = RetrofitClient.getApi();

        api.getRecipes().enqueue(new Callback<RecipeResponse>() {
            @Override
            public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
                showLoading(false);

                allRecipes = new ArrayList<>();

                if (response.isSuccessful() && response.body() != null) {
                    List<Recipe> apiRecipes = response.body().getRecipes();

                    if (apiRecipes != null && !apiRecipes.isEmpty()) {
                        allRecipes.addAll(apiRecipes);
                        Log.d(TAG, "Загружено из API: " + apiRecipes.size());
                    }
                }

                List<Recipe> localRecipes = recipeStorage.getLocalRecipes();
                if (localRecipes != null && !localRecipes.isEmpty()) {
                    allRecipes.addAll(localRecipes);
                    Log.d(TAG, "Загружено локальных: " + localRecipes.size());
                }

                if (allRecipes.isEmpty()) {
                    Log.d(TAG, "Нет рецептов! Добавляем тестовые...");
                    addTestRecipes();
                }

                if (!allRecipes.isEmpty()) {
                    Log.d(TAG, "Всего рецептов: " + allRecipes.size());
                    adapter.setData(allRecipes);
                    updateRecipeCount();
                    updateEmptyState();
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<RecipeResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Ошибка сети: " + t.getMessage());

                allRecipes = new ArrayList<>();

                List<Recipe> localRecipes = recipeStorage.getLocalRecipes();
                if (localRecipes != null && !localRecipes.isEmpty()) {
                    allRecipes.addAll(localRecipes);
                    Log.d(TAG, "Показаны локальные рецепты: " + localRecipes.size());
                }

                if (allRecipes.isEmpty()) {
                    Log.d(TAG, "Нет рецептов! Добавляем тестовые...");
                    addTestRecipes();
                }

                if (!allRecipes.isEmpty()) {
                    adapter.setData(allRecipes);
                    updateRecipeCount();
                    updateEmptyState();
                    Toast.makeText(MainActivity.this,
                            "Нет интернета. Показаны сохраненные рецепты",
                            Toast.LENGTH_SHORT).show();
                } else {
                    showError("Нет рецептов для отображения");
                }
            }
        });
    }

    private void addTestRecipes() {
        Recipe borsh = new Recipe();
        borsh.setId(1);
        borsh.setName("Борщ");
        borsh.setCategory("Первые блюда");
        borsh.setCookingTime(90);
        borsh.setDifficulty("Средняя");
        borsh.setDescription("Традиционный украинский суп");
        borsh.setImageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQMrgJV3_FEv9K_up8x-hDo2v4ivUuf3eKJrA&s");
        List<String> borshIngredients = new ArrayList<>();
        borshIngredients.add("свекла");
        borshIngredients.add("капуста");
        borshIngredients.add("картофель");
        borshIngredients.add("морковь");
        borshIngredients.add("лук");
        borshIngredients.add("мясо");
        borsh.setIngredients(borshIngredients);

        Recipe plov = new Recipe();
        plov.setId(2);
        plov.setName("Плов");
        plov.setCategory("Вторые блюда");
        plov.setCookingTime(120);
        plov.setDifficulty("Сложная");
        plov.setDescription("Восточное блюдо из риса и мяса");
        plov.setImageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQpk9wGpdXHiFxm_Za9lKih-g9es3zFdTFukw&s");
        List<String> plovIngredients = new ArrayList<>();
        plovIngredients.add("рис");
        plovIngredients.add("мясо");
        plovIngredients.add("морковь");
        plovIngredients.add("лук");
        plovIngredients.add("чеснок");
        plovIngredients.add("специи");
        plov.setIngredients(plovIngredients);

        Recipe caesar = new Recipe();
        caesar.setId(3);
        caesar.setName("Салат Цезарь");
        caesar.setCategory("Салаты");
        caesar.setCookingTime(20);
        caesar.setDifficulty("Легкая");
        caesar.setDescription("Классический итальянский салат");
        caesar.setImageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRc6jneChVyyJ2SHKCy22bDgeqpELk4VgqiCA&s");
        List<String> caesarIngredients = new ArrayList<>();
        caesarIngredients.add("салат");
        caesarIngredients.add("курица");
        caesarIngredients.add("сыр пармезан");
        caesarIngredients.add("гренки");
        caesarIngredients.add("соус");
        caesar.setIngredients(caesarIngredients);

        Recipe napoleon = new Recipe();
        napoleon.setId(4);
        napoleon.setName("Наполеон");
        napoleon.setCategory("Десерты");
        napoleon.setCookingTime(180);
        napoleon.setDifficulty("Сложная");
        napoleon.setDescription("Слоеный торт с кремом");
        napoleon.setImageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ5J1tLxxxcOiyCh3ehG5a4zlLGrSzxFxc8Bw&s");
        List<String> napoleonIngredients = new ArrayList<>();
        napoleonIngredients.add("мука");
        napoleonIngredients.add("масло");
        napoleonIngredients.add("яйца");
        napoleonIngredients.add("сахар");
        napoleonIngredients.add("молоко");
        napoleon.setIngredients(napoleonIngredients);

        allRecipes.add(borsh);
        allRecipes.add(plov);
        allRecipes.add(caesar);
        allRecipes.add(napoleon);

        Log.d(TAG, "Добавлено тестовых рецептов: 4");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recycler.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        showEmptyState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_RECIPE && resultCode == RESULT_OK) {
            loadRecipes();
            Toast.makeText(this, "Рецепт добавлен!", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_VIEW_RECIPE && resultCode == RESULT_OK) {
            // Обновляем список если изменилось избранное
            adapter.notifyDataSetChanged();
            updateRecipeCount();
        }
    }
}