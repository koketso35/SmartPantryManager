package com.richfield.smartpantrymanager.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.richfield.smartpantrymanager.R;
import com.richfield.smartpantrymanager.database.DatabaseHelper;
import com.richfield.smartpantrymanager.models.PantryItem;

import java.util.Calendar;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {

    private TextInputEditText etName, etQuantity, etUnit, etExpiry;
    private TextInputLayout tilExpiry;
    private MaterialButton btnSave, btnDelete, btnClearExpiry;
    private DatabaseHelper db;
    private long itemId = -1;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        db = DatabaseHelper.getInstance(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddEdit);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        etExpiry = findViewById(R.id.etExpiry);
        tilExpiry = findViewById(R.id.tilExpiry);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnClearExpiry = findViewById(R.id.btnClearExpiry);

        itemId = getIntent().getLongExtra("item_id", -1);
        isEdit = itemId != -1;

        toolbar.setTitle(isEdit ? getString(R.string.edit_ingredient) : getString(R.string.add_ingredient));
        toolbar.setNavigationOnClickListener(v -> finish());

        if (isEdit) {
            PantryItem item = db.getPantryItemById(itemId);
            if (item != null) {
                etName.setText(item.getName());
                etQuantity.setText(String.valueOf(item.getQuantity()));
                etUnit.setText(item.getUnit());
                if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
                    etExpiry.setText(item.getExpiryDate());
                }
            }
            btnDelete.setVisibility(View.VISIBLE);
            btnSave.setText(R.string.update);
        }

        // Open date picker when user taps the expiry field or the calendar icon
        View.OnClickListener openPicker = v -> showDatePicker();
        etExpiry.setOnClickListener(openPicker);
        tilExpiry.setEndIconOnClickListener(openPicker);

        btnClearExpiry.setOnClickListener(v -> etExpiry.setText(""));

        btnSave.setOnClickListener(v -> saveItem());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // If a date is already set, start the picker on that date
        String current = etExpiry.getText() != null ? etExpiry.getText().toString().trim() : "";
        if (current.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                String[] parts = current.split("-");
                calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            } catch (Exception ignored) {}
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // month is 0-based
                    String formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etExpiry.setText(formatted);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.setTitle("Select expiry date");
        dialog.show();
    }

    private void saveItem() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String qtyStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";
        String unit = etUnit.getText() != null ? etUnit.getText().toString().trim() : "";
        String expiry = etExpiry.getText() != null ? etExpiry.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.validation_name));
            etName.requestFocus();
            return;
        }
        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            etQuantity.setError(getString(R.string.validation_quantity));
            etQuantity.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(unit)) {
            etUnit.setError(getString(R.string.validation_unit));
            etUnit.requestFocus();
            return;
        }

        PantryItem item = new PantryItem(name, qty, unit, TextUtils.isEmpty(expiry) ? null : expiry);
        if (isEdit) {
            item.setId(itemId);
            db.updatePantryItem(item);
            Toast.makeText(this, R.string.updated_success, Toast.LENGTH_SHORT).show();
        } else {
            db.addPantryItem(item);
            Toast.makeText(this, R.string.added_success, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage(R.string.delete_confirm)
                .setPositiveButton(R.string.yes, (d, w) -> {
                    db.deletePantryItem(itemId);
                    Toast.makeText(this, R.string.deleted_success, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
