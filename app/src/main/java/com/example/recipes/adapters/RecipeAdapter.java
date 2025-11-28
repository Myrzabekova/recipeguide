package com.example.recipes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.R;
import com.example.recipes.models.Recipe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private List<Recipe> recipes = new ArrayList<>();
    private Context context;

    public RecipeAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.txtName.setText(recipe.getName());
        holder.txtCategory.setText(recipe.getCategory());
        holder.txtTime.setText(recipe.getCookingTime() + " мин.");

        // Загрузка изображения
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgRecipe);
        }
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRecipe;
        TextView txtName, txtCategory, txtTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecipe = itemView.findViewById(R.id.imgRecipe);
            txtName = itemView.findViewById(R.id.txtName);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}