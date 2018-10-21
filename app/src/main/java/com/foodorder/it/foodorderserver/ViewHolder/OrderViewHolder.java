package com.foodorder.it.foodorderserver.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.foodorder.it.foodorderserver.R;

import info.hoang8f.widget.FButton;

public class OrderViewHolder extends RecyclerView.ViewHolder{
    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAddress;

    public FButton btnEdit,btnRemove,btnDetails,btnDirection;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderAddress = itemView.findViewById(R.id.order_address);

        btnEdit = itemView.findViewById(R.id.btnEdit);
        btnRemove = itemView.findViewById(R.id.btnRemove);
        btnDetails = itemView.findViewById(R.id.btnDetails);
        btnDirection = itemView.findViewById(R.id.btnDirection);

    }
}

