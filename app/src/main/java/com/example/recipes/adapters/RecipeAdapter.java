package com.example.recipes.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.R;
import com.example.recipes.models.Recipe;
import com.example.recipes.utils.RecipeStorage;
import com.google.android.material.chip.Chip;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private List<Recipe> recipes = new ArrayList<>();
    private List<Recipe> recipesFull = new ArrayList<>();
    private Context context;
    private RecipeStorage recipeStorage;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(Context context, RecipeStorage recipeStorage) {
        this.context = context;
        this.recipeStorage = recipeStorage;
    }

    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Recipe> recipes) {
        this.recipes = recipes;
        this.recipesFull = new ArrayList<>(recipes);
        notifyDataSetChanged();
    }

    public void filter(String query, String category) {
        recipes.clear();

        if (query.isEmpty() && category.equals("Все")) {
            recipes.addAll(recipesFull);
        } else {
            for (Recipe recipe : recipesFull) {
                boolean matchesQuery = recipe.getName().toLowerCase()
                        .contains(query.toLowerCase());

                boolean matchesCategory;
                if (category.equals("Избранное")) {
                    matchesCategory = recipeStorage.isFavorite(recipe.getId());
                } else {
                    matchesCategory = category.equals("Все") ||
                            recipe.getCategory().equals(category);
                }

                if (matchesQuery && matchesCategory) {
                    recipes.add(recipe);
                }
            }
        }
        notifyDataSetChanged();
    }

    @ Override
    public int getItemCount() {
        return recipes.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.txtName.setText(recipe.getName());
        holder.chipCategory.setText(recipe.getCategory());
        holder.txtTime.setText(recipe.getCookingTimeString());

        if (recipe.getDescription() != null) {
            holder.txtDescription.setText(recipe.getDescription());
            holder.txtDescription.setVisibility(View.VISIBLE);
        } else {
            holder.txtDescription.setVisibility(View.GONE);
        }

        holder.txtIngredientsCount.setText(recipe.getIngredientsCountString());
        holder.chipDifficulty.setText(recipe.getDifficulty());
        setDifficultyColor(holder.chipDifficulty, recipe.getDifficulty());

        // ⭐ Показываем / скрываем значок избранного
        if (recipeStorage.isFavorite(recipe.getId())) {
            holder.favoriteIcon.setVisibility(View.VISIBLE);
        } else {
            holder.favoriteIcon.setVisibility(View.GONE);
        }

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .fit()
                    .centerCrop()
                    .into(holder.imgRecipe);
        } else {
            holder.imgRecipe.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRecipe, favoriteIcon;
        TextView txtName, txtDescription, txtTime, txtIngredientsCount;
        Chip chipCategory, chipDifficulty;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecipe = itemView.findViewById(R.id.imgRecipe);
            txtName = itemView.findViewById(R.id.txtName);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtIngredientsCount = itemView.findViewById(R.id.txtIngredientsCount);
            chipCategory = itemView.findViewById(R.id.chipCategory);
            chipDifficulty = itemView.findViewById(R.id.chipDifficulty);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }
    }
}
