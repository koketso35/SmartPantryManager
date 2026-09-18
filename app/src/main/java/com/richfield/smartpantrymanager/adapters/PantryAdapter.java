package com.richfield.smartpantrymanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantrymanager.R;
import com.richfield.smartpantrymanager.models.PantryItem;

import java.util.ArrayList;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private List<PantryItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public PantryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.tvName.setText(item.getName());
        String qtyText = formatQty(item.getQuantity()) + " " + item.getUnit();
        holder.tvQty.setText(qtyText);
        if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
            holder.tvExpiry.setVisibility(View.VISIBLE);
            holder.tvExpiry.setText("Expires: " + item.getExpiryDate());
        } else {
            holder.tvExpiry.setVisibility(View.GONE);
        }
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    private String formatQty(double q) {
        if (q == (long) q) return String.valueOf((long) q);
        return String.valueOf(q);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvExpiry;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvIngredientName);
            tvQty = itemView.findViewById(R.id.tvQuantity);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
