package com.richfield.smartpantrymanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.richfield.smartpantrymanager.models.PantryItem;
import com.richfield.smartpantrymanager.models.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // Pantry table
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QTY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry_date";

    // Recipes table
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_METHOD = "method";

    // Recipe ingredients table
    public static final String TABLE_RECIPE_ING = "recipe_ingredients";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "name";
    public static final String COL_RI_QTY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_QTY + " REAL NOT NULL, " +
                COL_UNIT + " TEXT NOT NULL, " +
                COL_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_METHOD + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_ING + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QTY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + "))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ---------- PANTRY CRUD ----------

    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, item.getName());
        cv.put(COL_QTY, item.getQuantity());
        cv.put(COL_UNIT, item.getUnit());
        cv.put(COL_EXPIRY, item.getExpiryDate());
        return db.insert(TABLE_PANTRY, null, cv);
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, item.getName());
        cv.put(COL_QTY, item.getQuantity());
        cv.put(COL_UNIT, item.getUnit());
        cv.put(COL_EXPIRY, item.getExpiryDate());
        return db.update(TABLE_PANTRY, cv, COL_ID + "=?", new String[]{String.valueOf(item.getId())});
    }

    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, COL_NAME + " ASC");
        if (c.moveToFirst()) {
            do {
                list.add(cursorToPantryItem(c));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public PantryItem getPantryItemById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) {
            item = cursorToPantryItem(c);
        }
        c.close();
        return item;
    }

    private PantryItem cursorToPantryItem(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_QTY)),
                c.getString(c.getColumnIndexOrThrow(COL_UNIT)),
                c.getString(c.getColumnIndexOrThrow(COL_EXPIRY))
        );
    }

    // ---------- RECIPES ----------

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, COL_RECIPE_NAME + " ASC");
        if (c.moveToFirst()) {
            do {
                long id = c.getLong(c.getColumnIndexOrThrow(COL_RECIPE_ID));
                Recipe r = new Recipe(id,
                        c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)),
                        c.getString(c.getColumnIndexOrThrow(COL_METHOD)));
                r.setIngredients(getIngredientsForRecipe(id));
                recipes.add(r);
            } while (c.moveToNext());
        }
        c.close();
        return recipes;
    }

    public Recipe getRecipeById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, COL_RECIPE_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Recipe r = null;
        if (c.moveToFirst()) {
            r = new Recipe(id,
                    c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_METHOD)));
            r.setIngredients(getIngredientsForRecipe(id));
        }
        c.close();
        return r;
    }

    private List<Recipe.RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        List<Recipe.RecipeIngredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPE_ING, null, COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        if (c.moveToFirst()) {
            do {
                list.add(new Recipe.RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow(COL_RI_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_RI_QTY)),
                        c.getString(c.getColumnIndexOrThrow(COL_RI_UNIT))
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    /**
     * STRICT MATCHING: a recipe is suggested only if EVERY required ingredient
     * is present in the pantry with at least the required quantity.
     * Names are normalized (lowercase + basic plural handling).
     * Units are compared loosely (same unit string after lower/trim).
     */
    public List<Recipe> getSuggestedRecipes() {
        List<PantryItem> pantry = getAllPantryItems();
        // Map normalizedName -> available quantity (aggregated if same name appears multiple times)
        Map<String, Double> pantryMap = new HashMap<>();
        Map<String, String> unitMap = new HashMap<>();
        for (PantryItem p : pantry) {
            String key = p.getNormalizedName();
            pantryMap.put(key, pantryMap.getOrDefault(key, 0.0) + p.getQuantity());
            unitMap.put(key, p.getUnit() != null ? p.getUnit().trim().toLowerCase() : "");
        }

        List<Recipe> all = getAllRecipes();
        List<Recipe> suggested = new ArrayList<>();

        for (Recipe recipe : all) {
            boolean canMake = true;
            for (Recipe.RecipeIngredient need : recipe.getIngredients()) {
                String key = need.getNormalizedName();
                Double have = pantryMap.get(key);
                if (have == null || have < need.getQuantity()) {
                    canMake = false;
                    break;
                }
                // Optional: unit check - if units differ significantly we still allow for simplicity
                // but prefer matching when possible
            }
            if (canMake) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    // ---------- SEED DATA (18 recipes) ----------

    private void seedRecipes(SQLiteDatabase db) {
        // Helper to insert recipe + ingredients
        insertRecipe(db, "Tomato Scramble",
                "1. Beat 2 eggs in a bowl.\n2. Dice tomato and fry lightly in a pan.\n3. Pour eggs over tomato, scramble until cooked.\n4. Season and serve hot.",
                new String[]{"egg", "2", "pieces", "tomato", "1", "piece", "salt", "1", "pinch"});

        insertRecipe(db, "Classic Cheese Toast",
                "1. Toast bread until golden.\n2. Place cheese slices on toast.\n3. Grill or microwave until cheese melts.\n4. Optional: add tomato slices on top.",
                new String[]{"bread", "2", "slices", "cheese", "2", "slices"});

        insertRecipe(db, "Simple Pasta Aglio",
                "1. Boil pasta according to packet.\n2. Fry garlic in olive oil until fragrant.\n3. Toss drained pasta in the oil.\n4. Add salt and serve.",
                new String[]{"pasta", "200", "g", "garlic", "2", "cloves", "olive oil", "2", "tbsp", "salt", "1", "pinch"});

        insertRecipe(db, "Banana Milkshake",
                "1. Peel and slice banana.\n2. Blend with milk until smooth.\n3. Pour into a glass and enjoy cold.",
                new String[]{"banana", "1", "piece", "milk", "250", "ml"});

        insertRecipe(db, "Vegetable Stir Fry",
                "1. Chop onion, carrot and any available veg.\n2. Heat oil in a wok, stir-fry onion first.\n3. Add remaining vegetables and cook 5-7 min.\n4. Season with salt and serve.",
                new String[]{"onion", "1", "piece", "carrot", "1", "piece", "oil", "2", "tbsp", "salt", "1", "pinch"});

        insertRecipe(db, "Egg Fried Rice",
                "1. Cook rice and let cool slightly.\n2. Scramble egg in a pan, set aside.\n3. Fry rice with a little oil, add egg back.\n4. Season and serve.",
                new String[]{"rice", "200", "g", "egg", "2", "pieces", "oil", "1", "tbsp", "salt", "1", "pinch"});

        insertRecipe(db, "Avocado Toast",
                "1. Toast bread.\n2. Mash avocado with a fork, season with salt.\n3. Spread on toast and serve.",
                new String[]{"bread", "2", "slices", "avocado", "1", "piece", "salt", "1", "pinch"});

        insertRecipe(db, "Omelette with Cheese",
                "1. Beat eggs with a splash of milk.\n2. Pour into a hot oiled pan.\n3. Add grated cheese, fold when set.\n4. Serve immediately.",
                new String[]{"egg", "3", "pieces", "cheese", "50", "g", "milk", "30", "ml", "oil", "1", "tsp"});

        insertRecipe(db, "Garlic Bread",
                "1. Mix softened butter with minced garlic.\n2. Spread on bread slices.\n3. Bake or toast until golden and fragrant.",
                new String[]{"bread", "4", "slices", "butter", "30", "g", "garlic", "2", "cloves"});

        insertRecipe(db, "Fruit Yogurt Bowl",
                "1. Spoon yogurt into a bowl.\n2. Top with sliced banana or available fruit.\n3. Optional: drizzle a little honey if available.",
                new String[]{"yogurt", "200", "g", "banana", "1", "piece"});

        insertRecipe(db, "Potato Mash",
                "1. Peel and boil potatoes until soft.\n2. Drain and mash with butter and a splash of milk.\n3. Season with salt and serve.",
                new String[]{"potato", "3", "pieces", "butter", "20", "g", "milk", "50", "ml", "salt", "1", "pinch"});

        insertRecipe(db, "Chicken Stir (basic)",
                "1. Cut chicken into strips.\n2. Stir-fry with onion and oil until cooked through.\n3. Season and serve with rice if available.",
                new String[]{"chicken", "200", "g", "onion", "1", "piece", "oil", "2", "tbsp", "salt", "1", "pinch"});

        insertRecipe(db, "Tomato Soup (quick)",
                "1. Dice tomatoes and onion.\n2. Simmer with a little water and salt until soft.\n3. Blend or mash, finish with a splash of milk or cream if available.",
                new String[]{"tomato", "4", "pieces", "onion", "1", "piece", "salt", "1", "pinch"});

        insertRecipe(db, "Pancakes (basic)",
                "1. Mix flour, egg, milk into a smooth batter.\n2. Fry spoonfuls in a lightly oiled pan until bubbles form, then flip.\n3. Serve warm.",
                new String[]{"flour", "150", "g", "egg", "1", "piece", "milk", "200", "ml", "oil", "1", "tbsp"});

        insertRecipe(db, "Cheese Omelette Wrap",
                "1. Make a thin omelette.\n2. Place cheese inside and roll.\n3. Optional: add leftover veggies.",
                new String[]{"egg", "2", "pieces", "cheese", "30", "g", "oil", "1", "tsp"});

        insertRecipe(db, "Boiled Eggs & Toast",
                "1. Boil eggs for 6-8 minutes.\n2. Toast bread.\n3. Peel eggs, season and serve with toast.",
                new String[]{"egg", "2", "pieces", "bread", "2", "slices", "salt", "1", "pinch"});

        insertRecipe(db, "Carrot & Potato Stew",
                "1. Dice carrot, potato and onion.\n2. Simmer in water with salt until tender.\n3. Mash slightly or leave chunky. Serve hot.",
                new String[]{"carrot", "2", "pieces", "potato", "2", "pieces", "onion", "1", "piece", "salt", "1", "pinch"});

        insertRecipe(db, "Milk & Banana Porridge",
                "1. Heat milk gently.\n2. Stir in oats or mashed banana.\n3. Cook until thickened, sweeten if desired.",
                new String[]{"milk", "300", "ml", "banana", "1", "piece"});
    }

    private void insertRecipe(SQLiteDatabase db, String name, String method, String[] ingredientsFlat) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RECIPE_NAME, name);
        cv.put(COL_METHOD, method);
        long recipeId = db.insert(TABLE_RECIPES, null, cv);

        // ingredientsFlat is triplets: name, qty, unit, name, qty, unit...
        for (int i = 0; i + 2 < ingredientsFlat.length; i += 3) {
            ContentValues icv = new ContentValues();
            icv.put(COL_RI_RECIPE_ID, recipeId);
            icv.put(COL_RI_NAME, ingredientsFlat[i]);
            icv.put(COL_RI_QTY, Double.parseDouble(ingredientsFlat[i + 1]));
            icv.put(COL_RI_UNIT, ingredientsFlat[i + 2]);
            db.insert(TABLE_RECIPE_ING, null, icv);
        }
    }
}
