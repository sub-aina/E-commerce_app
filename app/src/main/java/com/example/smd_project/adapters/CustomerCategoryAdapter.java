package com.example.smd_project.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.models.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CustomerCategoryAdapter extends RecyclerView.Adapter<CustomerCategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;
    private OnCategoryClickListener clickListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CustomerCategoryAdapter(Context context, List<Category> categoryList,
                                   OnCategoryClickListener clickListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_customer_category, parent, false);
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
        private TextView tvCategoryName, tvProductCount;
        private ImageView ivCategoryIcon;
        private CardView cardCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvProductCount = itemView.findViewById(R.id.tvProductCount);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            cardCategory = itemView.findViewById(R.id.cardCategory);
        }

        public void bind(Category category) {
            tvCategoryName.setText(category.getName());


            if (tvProductCount != null) {
                loadProductCount(category.getCategoryId());
            }


            cardCategory.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onCategoryClick(category);
                }
            });

        }

        private void loadProductCount(String categoryId) {
            FirebaseHelper.getInstance().getAllProductsReference()
                    .orderByChild("category")
                    .equalTo(categoryId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int count = (int) snapshot.getChildrenCount();
                            tvProductCount.setText(count + " products");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            tvProductCount.setText("0 products");
                        }
                    });
        }
    }
}
