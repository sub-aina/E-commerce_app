package com.example.smd_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.R;
import com.example.smd_project.models.Category;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;
    private OnCategoryClickListener clickListener; // NEW

    public interface OnEditClickListener {
        void onEdit(Category category);
    }

    public interface OnDeleteClickListener {
        void onDelete(Category category);
    }

    // NEW: Interface for category click
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categoryList,
                           OnEditClickListener editListener,
                           OnDeleteClickListener deleteListener) {
        this.categoryList = categoryList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }


    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvCategoryDescription, tvCreatedAt;
        ImageButton btnEdit, btnDelete;
        CardView cardCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryDescription = itemView.findViewById(R.id.tvCategoryDescription);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
//            cardCategory = itemView.findViewById(R.id.cardCategory);
        }

        public void bind(Category category) {
            tvCategoryName.setText(category.getName());

            if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                tvCategoryDescription.setText(category.getDescription());
                tvCategoryDescription.setVisibility(View.VISIBLE);
            } else {
                tvCategoryDescription.setVisibility(View.GONE);
            }


            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String date = sdf.format(new Date(category.getCreatedAt()));
            tvCreatedAt.setText("Created: " + date);

            // Edit and Delete buttons
            btnEdit.setOnClickListener(v -> editListener.onEdit(category));
            btnDelete.setOnClickListener(v -> deleteListener.onDelete(category));


            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onCategoryClick(category);
                }
            });
        }
    }
}