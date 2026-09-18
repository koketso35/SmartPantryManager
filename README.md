# Smart Pantry Manager

**A Java Android application that suggests recipes based strictly on leftover ingredients to cut food waste.**

## Overview

Smart Pantry Manager helps users reduce food waste by tracking the ingredients they already have at home and suggesting only those recipes they can cook *right now* — no shopping trip required.

The core business rule is **strict matching**: a recipe appears in the “Suggested Recipes” list only when **every** required ingredient is present in the pantry in at least the required quantity. Partial matches are never shown in the main suggestions list.

## Features

- **Pantry management** – Add, edit and delete ingredients (name, quantity, unit, optional expiry date)
- **Pantry list** – RecyclerView of all current items with edit/delete actions
- **Recipe collection** – 18 pre-seeded recipes with ingredients and preparation steps
- **Suggested Recipes** – Strict-matching engine that only lists fully cookable recipes
- **Recipe detail** – Full ingredient list + method
- **Settings** – Expiring-soon alerts toggle and preferred units
- **Persistent storage** – SQLite database (data survives app restarts)
- **Input validation** on all forms
- **Bottom navigation** between Pantry / Suggested / All Recipes / Settings

## Database Choice: SQLite

This project uses **SQLite** via `SQLiteOpenHelper` because:

- It matches the module’s persistent-data content
- Fully offline / on-device – no network or third-party accounts required
- Simple CRUD and easy to demonstrate data survival after close/reopen
- Sufficient for the scale of a personal pantry + recipe set

## Requirements

- Android Studio Hedgehog (2023.1.1) or later recommended
- JDK 8+
- Min SDK 24, Target SDK 34
- Emulator or physical device running Android 7.0+

## How to Run

1. Open the project folder in Android Studio (`File → Open`).
2. Let Gradle sync (internet required the first time).
3. Select an emulator or connected device.
4. Click **Run** (green play button).

On first launch the database is created and 18 recipes are automatically seeded.

## Project Structure

```
app/src/main/java/com/richfield/smartpantrymanager/
├── activities/
│   ├── MainActivity.java              # Host + bottom nav + all sections
│   ├── AddEditIngredientActivity.java # Create / Update / Delete pantry item
│   └── RecipeDetailActivity.java      # Recipe ingredients + method
├── adapters/
│   ├── PantryAdapter.java
│   └── RecipeAdapter.java
├── database/
│   └── DatabaseHelper.java            # SQLite + strict matching + seed data
└── models/
    ├── PantryItem.java
    └── Recipe.java
```

## Strict-Matching Logic (core)

Located in `DatabaseHelper.getSuggestedRecipes()`:

1. Build a map of normalised pantry ingredient names → total quantity.
2. For each recipe, check that **every** required ingredient exists in the map with quantity ≥ required.
3. Name normalisation: lowercase + basic singular/plural handling (`tomatoes` → `tomato`, `berries` → `berry`).
4. Only fully matching recipes are returned.

## Colour Theme

- Primary Blue `#1565C0`
- Accent Red `#D32F2F`
- White / Off-white backgrounds

## Author

Student project for **Mobile App Development 700** – Richfield Graduate Institute of Technology.

## License

Academic use only.
