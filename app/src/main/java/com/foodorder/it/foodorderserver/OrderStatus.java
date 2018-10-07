package com.foodorder.it.foodorderserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.foodorder.it.foodorderserver.Common.Common;
import com.foodorder.it.foodorderserver.Interface.ItemClickListener;
import com.foodorder.it.foodorderserver.Model.Request;
import com.foodorder.it.foodorderserver.ViewHolder.OrderViewHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class OrderStatus extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseDatabase database;
    private DatabaseReference request;

    private FirebaseRecyclerAdapter<Request , OrderViewHolder> adapter;

    private MaterialSpinner status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //init firebase
        database = FirebaseDatabase.getInstance();
        request = database.getReference("Requests");

        //setup recycler view
        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        LoadRequests();
    }

    private void LoadRequests() {

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(Request.class,R.layout.order_layout,OrderViewHolder.class,request) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.txtOrderAddress.setText(model.getAddress());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent tracking = new Intent(OrderStatus.this,TrackingOrder.class);
                        Common.CurrentRequest = model;
                        startActivity(tracking);
                    }
                });

            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE))
            updateStatus(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));

            else if(item.getTitle().equals(Common.DELETE))
               request.child(adapter.getRef(item.getOrder()).getKey()).removeValue();
        return super.onContextItemSelected(item);
    }

    private void updateStatus(final String key, final Request item) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Update Status");
        alertDialog.setMessage("Select Status");
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        LayoutInflater inflater = LayoutInflater.from(this);
        View update_status_layout = inflater.inflate(R.layout.update_order_layout, null);

        status = update_status_layout.findViewById(R.id.statusSpinner);
        status.setItems("Placed","On My Way","Shipped");

        alertDialog.setView(update_status_layout);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
                item.setStatus(String.valueOf(status.getSelectedIndex()));
                request.child(key).setValue(item);
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }
}
