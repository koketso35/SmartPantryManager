package com.richfield.smartpantrymanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.richfield.smartpantrymanager.R;
import com.richfield.smartpantrymanager.adapters.PantryAdapter;
import com.richfield.smartpantrymanager.adapters.RecipeAdapter;
import com.richfield.smartpantrymanager.database.DatabaseHelper;
import com.richfield.smartpantrymanager.models.PantryItem;
import com.richfield.smartpantrymanager.models.Recipe;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FrameLayout contentFrame;
    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;
    private DatabaseHelper db;
    private View pantryView, suggestedView, recipesView, settingsView;

    private PantryAdapter pantryAdapter;
    private RecipeAdapter suggestedAdapter, allRecipesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseHelper.getInstance(this);
        contentFrame = findViewById(R.id.contentFrame);
        bottomNav = findViewById(R.id.bottomNav);
        toolbar = findViewById(R.id.toolbar);

        // Inflate section layouts once
        LayoutInflater inflater = LayoutInflater.from(this);
        pantryView = inflater.inflate(R.layout.layout_pantry, contentFrame, false);
        suggestedView = inflater.inflate(R.layout.layout_suggested, contentFrame, false);
        recipesView = inflater.inflate(R.layout.layout_all_recipes, contentFrame, false);
        settingsView = inflater.inflate(R.layout.layout_settings, contentFrame, false);

        setupPantrySection();
        setupSuggestedSection();
        setupAllRecipesSection();
        setupSettingsSection();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                showSection(pantryView, "My Pantry");
                refreshPantry();
                return true;
            } else if (id == R.id.nav_suggested) {
                showSection(suggestedView, "Suggested Recipes");
                refreshSuggested();
                return true;
            } else if (id == R.id.nav_recipes) {
                showSection(recipesView, "All Recipes");
                refreshAllRecipes();
                return true;
            } else if (id == R.id.nav_settings) {
                showSection(settingsView, "Settings");
                return true;
            }
            return false;
        });

        // Default to pantry
        bottomNav.setSelectedItemId(R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh current section when returning from Add/Edit or Detail
        if (bottomNav.getSelectedItemId() == R.id.nav_pantry) {
            refreshPantry();
        } else if (bottomNav.getSelectedItemId() == R.id.nav_suggested) {
            refreshSuggested();
        }
    }

    private void showSection(View view, String title) {
        contentFrame.removeAllViews();
        contentFrame.addView(view);
        toolbar.setTitle(title);
    }

    // ---------- PANTRY ----------
    private void setupPantrySection() {
        RecyclerView rv = pantryView.findViewById(R.id.rvPantry);
        rv.setLayoutManager(new LinearLayoutManager(this));
        pantryAdapter = new PantryAdapter(new PantryAdapter.OnItemClickListener() {
            @Override
            public void onEdit(PantryItem item) {
                Intent i = new Intent(MainActivity.this, AddEditIngredientActivity.class);
                i.putExtra("item_id", item.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(PantryItem item) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete")
                        .setMessage(getString(R.string.delete_confirm))
                        .setPositiveButton(R.string.yes, (d, w) -> {
                            db.deletePantryItem(item.getId());
                            Toast.makeText(MainActivity.this, R.string.deleted_success, Toast.LENGTH_SHORT).show();
                            refreshPantry();
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        });
        rv.setAdapter(pantryAdapter);

        FloatingActionButton fab = pantryView.findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            startActivity(i);
        });
    }

    private void refreshPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        pantryAdapter.setItems(items);
        TextView count = pantryView.findViewById(R.id.tvPantryCount);
        TextView empty = pantryView.findViewById(R.id.tvEmptyPantry);
        RecyclerView rv = pantryView.findViewById(R.id.rvPantry);
        count.setText(items.size() + " item" + (items.size() != 1 ? "s" : ""));
        if (items.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }
    }

    // ---------- SUGGESTED ----------
    private void setupSuggestedSection() {
        RecyclerView rv = suggestedView.findViewById(R.id.rvSuggested);
        rv.setLayoutManager(new LinearLayoutManager(this));
        suggestedAdapter = new RecipeAdapter(recipe -> openRecipeDetail(recipe), true);
        rv.setAdapter(suggestedAdapter);
    }

    private void refreshSuggested() {
        List<Recipe> suggested = db.getSuggestedRecipes();
        suggestedAdapter.setRecipes(suggested);
        TextView count = suggestedView.findViewById(R.id.tvSuggestedCount);
        TextView empty = suggestedView.findViewById(R.id.tvEmptySuggested);
        RecyclerView rv = suggestedView.findViewById(R.id.rvSuggested);
        count.setText(getString(R.string.matches_count, suggested.size()));
        if (suggested.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }
    }

    // ---------- ALL RECIPES ----------
    private void setupAllRecipesSection() {
        RecyclerView rv = recipesView.findViewById(R.id.rvAllRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        allRecipesAdapter = new RecipeAdapter(recipe -> openRecipeDetail(recipe), false);
        rv.setAdapter(allRecipesAdapter);
    }

    private void refreshAllRecipes() {
        List<Recipe> all = db.getAllRecipes();
        allRecipesAdapter.setRecipes(all);
        TextView count = recipesView.findViewById(R.id.tvAllRecipesCount);
        count.setText(all.size() + " recipes available");
    }

    private void openRecipeDetail(Recipe recipe) {
        Intent i = new Intent(this, RecipeDetailActivity.class);
        i.putExtra("recipe_id", recipe.getId());
        startActivity(i);
    }

    // ---------- SETTINGS ----------
    private void setupSettingsSection() {
        SwitchMaterial sw = settingsView.findViewById(R.id.switchExpiryAlerts);
        // Persist preference simply with SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        sw.setChecked(prefs.getBoolean("expiry_alerts", true));
        sw.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("expiry_alerts", isChecked).apply());

        Spinner spinner = settingsView.findViewById(R.id.spinnerUnits);
        String[] units = {"Metric (g, ml)", "Imperial (oz, cups)", "Mixed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, units);
        spinner.setAdapter(adapter);
        int selected = prefs.getInt("units_pref", 0);
        spinner.setSelection(selected);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt("units_pref", position).apply();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }
}
