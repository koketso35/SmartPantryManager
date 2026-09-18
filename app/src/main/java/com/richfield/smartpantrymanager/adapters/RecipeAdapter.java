package com.richfield.smartpantrymanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantrymanager.R;
import com.richfield.smartpantrymanager.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private List<Recipe> recipes = new ArrayList<>();
    private final OnRecipeClickListener listener;
    private final boolean showMatchBadge;

    public RecipeAdapter(OnRecipeClickListener listener, boolean showMatchBadge) {
        this.listener = listener;
        this.showMatchBadge = showMatchBadge;
    }

    public void setRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes != null ? newRecipes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe r = recipes.get(position);
        holder.tvName.setText(r.getName());
        int count = r.getIngredients() != null ? r.getIngredients().size() : 0;
        holder.tvInfo.setText(count + " ingredient" + (count != 1 ? "s" : ""));
        if (showMatchBadge) {
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.tvBadge.setText("You can make this!");
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(r));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo, tvBadge;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvInfo = itemView.findViewById(R.id.tvRecipeInfo);
            tvBadge = itemView.findViewById(R.id.tvMatchBadge);
        }
    }
}
