package com.richfield.smartpantrymanager.models;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private long id;
    private String name;
    private String method;
    private List<RecipeIngredient> ingredients;

    public Recipe() {
        ingredients = new ArrayList<>();
    }

    public Recipe(String name, String method) {
        this.name = name;
        this.method = method;
        this.ingredients = new ArrayList<>();
    }

    public Recipe(long id, String name, String method) {
        this.id = id;
        this.name = name;
        this.method = method;
        this.ingredients = new ArrayList<>();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }

    public void addIngredient(RecipeIngredient ri) {
        if (ingredients == null) ingredients = new ArrayList<>();
        ingredients.add(ri);
    }

    public static class RecipeIngredient {
        private String name;
        private double quantity;
        private String unit;

        public RecipeIngredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getName() { return name; }
        public double getQuantity() { return quantity; }
        public String getUnit() { return unit; }

        public String getNormalizedName() {
            if (name == null) return "";
            String n = name.trim().toLowerCase();
            if (n.endsWith("oes")) n = n.substring(0, n.length() - 2);
            else if (n.endsWith("ies")) n = n.substring(0, n.length() - 3) + "y";
            else if (n.endsWith("s") && !n.endsWith("ss") && n.length() > 3) n = n.substring(0, n.length() - 1);
            return n;
        }
    }
}
