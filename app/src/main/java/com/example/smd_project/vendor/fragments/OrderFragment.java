package com.example.smd_project.vendor.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.adapters.OrderAdapter;
import com.example.smd_project.models.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderFragment extends Fragment implements OrderAdapter.OnOrderActionListener {

    private RecyclerView recyclerView;
    private Spinner statusSpinner;
    private OrderAdapter orderAdapter;
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();
    private String currentStatusFilter = "All";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        recyclerView = view.findViewById(R.id.recyclerOrders);
        statusSpinner = view.findViewById(R.id.spinnerOrderStatus);

        setupRecyclerView();
        setupSpinner();
        loadOrders();

        return view;
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(filteredOrders, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(orderAdapter);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.order_status_filter,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(spinnerAdapter);

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentStatusFilter = parent.getItemAtPosition(position).toString();
                filterOrders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadOrders() {
        FirebaseHelper.getInstance().getAllOrdersReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrders.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Order order = snap.getValue(Order.class);
                    if (order != null) {
                        order.setOrderId(snap.getKey());
                        allOrders.add(order);
                    }
                }
                Collections.reverse(allOrders);
                filterOrders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterOrders() {
        filteredOrders.clear();
        if (currentStatusFilter.equals("All")) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (order.getStatus() != null && order.getStatus().equalsIgnoreCase(currentStatusFilter)) {
                    filteredOrders.add(order);
                }
            }
        }
        orderAdapter.notifyDataSetChanged();
    }

    // ==================== OrderAdapter.OnOrderActionListener Implementation ====================

    @Override
    public void onStatusChange(String orderId, String newStatus) {
        FirebaseHelper.getInstance().updateOrderStatus(orderId, newStatus, new FirebaseHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Order " + orderId + " updated to " + newStatus, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Status update failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onViewDetails(Order order) {
        Log.d("OrderFragment", "onViewDetails called for order: " + order.getOrderId());

        // Create the detail fragment
        Fragment detailsFragment = OrdersDetailFragment.newInstance(order);

        // Get the parent container ID (the container that holds THIS fragment)
        int containerId = ((ViewGroup) requireView().getParent()).getId();

        Log.d("OrderFragment", "Container ID: " + containerId);

        // Replace this fragment with the details fragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(containerId, detailsFragment)
                .addToBackStack(null)
                .commit();

        Log.d("OrderFragment", "Transaction committed successfully");
    }
}