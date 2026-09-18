package com.richfield.smartpantrymanager.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.richfield.smartpantrymanager.R;
import com.richfield.smartpantrymanager.database.DatabaseHelper;
import com.richfield.smartpantrymanager.models.Recipe;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbarDetail);
        toolbar.setTitle(R.string.recipe_detail);
        toolbar.setNavigationOnClickListener(v -> finish());

        long recipeId = getIntent().getLongExtra("recipe_id", -1);
        if (recipeId == -1) {
            finish();
            return;
        }

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        Recipe recipe = db.getRecipeById(recipeId);
        if (recipe == null) {
            finish();
            return;
        }

        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvCanMake = findViewById(R.id.tvCanMake);
        LinearLayout llIngredients = findViewById(R.id.llIngredients);
        TextView tvMethod = findViewById(R.id.tvMethod);

        tvName.setText(recipe.getName());
        tvMethod.setText(recipe.getMethod());

        // Check if currently suggested
        List<Recipe> suggested = db.getSuggestedRecipes();
        boolean canMake = false;
        for (Recipe s : suggested) {
            if (s.getId() == recipe.getId()) {
                canMake = true;
                break;
            }
        }
        tvCanMake.setVisibility(canMake ? View.VISIBLE : View.GONE);

        // Populate ingredients
        llIngredients.removeAllViews();
        if (recipe.getIngredients() != null) {
            for (Recipe.RecipeIngredient ri : recipe.getIngredients()) {
                TextView tv = new TextView(this);
                String qtyStr = (ri.getQuantity() == (long) ri.getQuantity())
                        ? String.valueOf((long) ri.getQuantity())
                        : String.valueOf(ri.getQuantity());
                tv.setText("•  " + qtyStr + " " + ri.getUnit() + "  " + ri.getName());
                tv.setTextSize(15f);
                tv.setTextColor(getResources().getColor(R.color.dark_gray, null));
                tv.setPadding(0, 6, 0, 6);
                llIngredients.addView(tv);
            }
        }
    }
}
