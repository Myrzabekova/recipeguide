package com.example.recipes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.recipes.models.Recipe;
import com.example.recipes.utils.RecipeStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private TextInputEditText etRecipeName;
    private AutoCompleteTextView spinnerCategory;
    private AutoCompleteTextView spinnerDifficulty;
    private TextInputEditText etCookingTime;
    private TextInputEditText etImageUrl;
    private TextInputEditText etDescription;
    private TextInputEditText etIngredients;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private Toolbar toolbar;

    private RecipeStorage recipeStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        recipeStorage = new RecipeStorage(this);

        initViews();
        setupToolbar();
        setupSpinners();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etRecipeName = findViewById(R.id.etRecipeName);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        etCookingTime = findViewById(R.id.etCookingTime);
        etImageUrl = findViewById(R.id.etImageUrl);
        etDescription = findViewById(R.id.etDescription);
        etIngredients = findViewById(R.id.etIngredients);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSpinners() {
        // Категории
        String[] categories = {"Первые блюда", "Вторые блюда", "Салаты", "Десерты", "Закуски"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        spinnerCategory.setAdapter(categoryAdapter);

        // Сложность
        String[] difficulties = {"Легкая", "Средняя", "Сложная"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                difficulties
        );
        spinnerDifficulty.setAdapter(difficultyAdapter);
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> saveRecipe());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveRecipe() {
        // Получаем данные из полей
        String name = getText(etRecipeName);
        String category = spinnerCategory.getText().toString().trim();
        String difficulty = spinnerDifficulty.getText().toString().trim();
        String cookingTimeStr = getText(etCookingTime);
        String imageUrl = getText(etImageUrl);
        String description = getText(etDescription);
        String ingredientsStr = getText(etIngredients);

        // Валидация обязательных полей
        if (TextUtils.isEmpty(name)) {
            etRecipeName.setError("Введите название рецепта");
            etRecipeName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Выберите категорию", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(difficulty)) {
            Toast.makeText(this, "Выберите сложность", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(cookingTimeStr)) {
            etCookingTime.setError("Введите время приготовления");
            etCookingTime.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(ingredientsStr)) {
            etIngredients.setError("Введите ингредиенты");
            etIngredients.requestFocus();
            return;
        }

        // Парсинг времени приготовления
        int cookingTime;
        try {
            cookingTime = Integer.parseInt(cookingTimeStr);
            if (cookingTime <= 0) {
                etCookingTime.setError("Время должно быть больше 0");
                etCookingTime.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etCookingTime.setError("Введите корректное число");
            etCookingTime.requestFocus();
            return;
        }

        // Парсинг ингредиентов (разделение по строкам)
        List<String> ingredients = new ArrayList<>();
        String[] ingredientsArray = ingredientsStr.split("\n");
        for (String ingredient : ingredientsArray) {
            String trimmed = ingredient.trim();
            if (!trimmed.isEmpty()) {
                ingredients.add(trimmed);
            }
        }

        if (ingredients.isEmpty()) {
            etIngredients.setError("Добавьте хотя бы один ингредиент");
            etIngredients.requestFocus();
            return;
        }

        // URL по умолчанию, если не указан
        if (TextUtils.isEmpty(imageUrl)) {
            imageUrl = "https://via.placeholder.com/400x300?text=Рецепт";
        }

        // Создание объекта рецепта
        Recipe newRecipe = recipeStorage.createRecipe(
                name,
                category,
                cookingTime,
                difficulty,
                ingredients,
                description,
                imageUrl
        );

        // Сохранение рецепта
        boolean saved = recipeStorage.addRecipe(newRecipe);

        if (saved) {
            Toast.makeText(this, "Рецепт успешно добавлен!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Ошибка при сохранении рецепта", Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}