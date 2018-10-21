package com.foodorder.it.foodorderserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.foodorder.it.foodorderserver.Common.Common;
import com.foodorder.it.foodorderserver.Model.Notification;
import com.foodorder.it.foodorderserver.Model.Request;
import com.foodorder.it.foodorderserver.Model.Send_TO;
import com.foodorder.it.foodorderserver.Model.Token;
import com.foodorder.it.foodorderserver.Remote.APIService;
import com.foodorder.it.foodorderserver.ViewHolder.OrderViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.List;

public class OrderStatus extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseDatabase database;
    private DatabaseReference request;

    private FirebaseRecyclerAdapter<Request , OrderViewHolder> adapter;

    private MaterialSpinner status;

    private APIService mService;


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
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.txtOrderAddress.setText(model.getAddress());

                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateStatus(adapter.getRef(position).getKey(), adapter.getItem(position));
                    }
                });

                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        request.child(adapter.getRef(position).getKey()).removeValue();
                        adapter.notifyDataSetChanged();
                    }
                });

                viewHolder.btnDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent showDetails = new Intent(getBaseContext(),OrderDetails.class);
                        Common.CurrentRequest = model;
                        showDetails.putExtra("orderId",adapter.getRef(position).getKey());
                        startActivity(showDetails);
                    }
                });

                viewHolder.btnDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent trackingIntent = new Intent(getBaseContext(),TrackingOrder.class);
                        Common.CurrentRequest = model;
                        startActivity(trackingIntent);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
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

                adapter.notifyDataSetChanged();
                sendOrderStatusToUser(key,item); // notification
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void sendOrderStatusToUser(final String key, Request item) {

        DatabaseReference tokens = database.getReference("Tokens");
        tokens.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                        {
                            Token token = dataSnapshot1.getValue(Token.class);

                            //Make row payload
                            Notification notification = new Notification("peter samuel","Your order "+key+" was updated");
                            Send_TO send = new Send_TO(token.getToken(), (List<Notification>) notification);

                            mService.sendNotification(send);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
}
