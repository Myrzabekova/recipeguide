package com.example.recipes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipes.models.Recipe;
import com.example.recipes.utils.RecipeStorage;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.squareup.picasso.Picasso;

public class RecipeDetailActivity extends AppCompatActivity {

    private Recipe recipe;
    private RecipeStorage recipeStorage;

    private ImageView recipeImage;
    private TextView recipeName;
    private TextView recipeDescription;
    private TextView cookingTime;
    private Chip categoryChip;
    private Chip difficultyChip;
    private LinearLayout ingredientsContainer;
    private MaterialButton btnShare;
    private MaterialButton btnFavorite;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeStorage = new RecipeStorage(this);

        recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        if (recipe == null) {
            Toast.makeText(this, "Ошибка загрузки рецепта", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        displayRecipeDetails();
        setupButtons();
    }

    private void initViews() {
        recipeImage = findViewById(R.id.recipeImage);
        recipeName = findViewById(R.id.recipeName);
        recipeDescription = findViewById(R.id.recipeDescription);
        cookingTime = findViewById(R.id.cookingTime);
        categoryChip = findViewById(R.id.categoryChip);
        difficultyChip = findViewById(R.id.difficultyChip);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        btnShare = findViewById(R.id.btnShare);
        btnFavorite = findViewById(R.id.btnFavorite);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            // Возвращаем результат что изменилось избранное
            Intent resultIntent = new Intent();
            resultIntent.putExtra("favorite_changed", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        collapsingToolbar.setTitle(recipe.getName());
    }

    private void displayRecipeDetails() {
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .fit()
                    .centerCrop()
                    .into(recipeImage);
        }

        recipeName.setText(recipe.getName());

        if (recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
            recipeDescription.setText(recipe.getDescription());
        } else {
            recipeDescription.setVisibility(View.GONE);
        }

        cookingTime.setText(recipe.getCookingTime() + " минут");
        categoryChip.setText(recipe.getCategory());
        difficultyChip.setText(recipe.getDifficulty());
        setDifficultyColor(difficultyChip, recipe.getDifficulty());

        displayIngredients();
    }

    private void displayIngredients() {
        ingredientsContainer.removeAllViews();

        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            for (String ingredient : recipe.getIngredients()) {
                View ingredientView = getLayoutInflater()
                        .inflate(R.layout.item_ingredient, ingredientsContainer, false);

                TextView ingredientText = ingredientView.findViewById(R.id.ingredientText);
                ingredientText.setText("• " + ingredient);

                ingredientsContainer.addView(ingredientView);
            }
        }
    }

    private void setDifficultyColor(Chip chip, String difficulty) {
        int color;
        switch (difficulty) {
            case "Легкая":
                color = Color.parseColor("#4CAF50");
                break;
            case "Средняя":
                color = Color.parseColor("#FF9800");
                break;
            case "Сложная":
                color = Color.parseColor("#F44336");
                break;
            default:
                color = Color.parseColor("#9E9E9E");
                break;
        }
        chip.setChipBackgroundColor(
                android.content.res.ColorStateList.valueOf(color));
        chip.setTextColor(Color.WHITE);
    }

    private void setupButtons() {
        btnShare.setOnClickListener(v -> shareRecipe());

        // Устанавливаем начальное состояние кнопки
        updateFavoriteButton();

        btnFavorite.setOnClickListener(v -> {
            recipeStorage.toggleFavorite(recipe.getId());
            updateFavoriteButton();

            String message = recipeStorage.isFavorite(recipe.getId())
                    ? "⭐ Добавлено в избранное!"
                    : "Удалено из избранного";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateFavoriteButton() {
        boolean isFavorite = recipeStorage.isFavorite(recipe.getId());

        if (isFavorite) {
            btnFavorite.setText("❤️ Удалить из избранного");
            btnFavorite.setBackgroundColor(Color.parseColor("#F44336")); // Красный
            btnFavorite.setIcon(getResources().getDrawable(android.R.drawable.star_big_on));
        } else {
            btnFavorite.setText("🤍 В избранное");
            btnFavorite.setBackgroundColor(Color.parseColor("#FF5722")); // Оранжевый
            btnFavorite.setIcon(getResources().getDrawable(android.R.drawable.btn_star_big_on));
        }
    }

    private void shareRecipe() {
        StringBuilder shareText = new StringBuilder();
        shareText.append("Рецепт: ").append(recipe.getName()).append("\n\n");

        if (recipe.getDescription() != null) {
            shareText.append(recipe.getDescription()).append("\n\n");
        }

        shareText.append("Время приготовления: ")
                .append(recipe.getCookingTime()).append(" мин\n");
        shareText.append("Сложность: ").append(recipe.getDifficulty()).append("\n\n");

        shareText.append("Ингредиенты:\n");
        if (recipe.getIngredients() != null) {
            for (String ingredient : recipe.getIngredients()) {
                shareText.append("• ").append(ingredient).append("\n");
            }
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Рецепт: " + recipe.getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());

        startActivity(Intent.createChooser(shareIntent, "Поделиться рецептом"));
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("favorite_changed", true);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }
}