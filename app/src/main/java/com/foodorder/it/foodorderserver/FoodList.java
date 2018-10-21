package com.foodorder.it.foodorderserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.foodorder.it.foodorderserver.Common.Common;
import com.foodorder.it.foodorderserver.Model.Food;
import com.foodorder.it.foodorderserver.ViewHolder.FoodViewHolder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import info.hoang8f.widget.FButton;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foods;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseRecyclerAdapter<Food,FoodViewHolder> foodAdapter;

    FloatingActionButton fab;
    //for add new food dialog
    MaterialEditText edtFoodName,edtFoodDescription,edtFoodPrice,edtFoodDiscount;
    FButton btnUpload, btnSelect;

    RelativeLayout rootLayout;


    private String categoryId = "";
    private Uri imageUri;
    private Food newFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout =findViewById(R.id.root_layout);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowAddFoodDialog();
            }
        });

        //init firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if(getIntent()!=null)
            categoryId = getIntent().getStringExtra("categoryId");

        LoadFoods();
    }

    private void LoadFoods() {

        foodAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foods.orderByChild("menuId").equalTo(categoryId)) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.txtFoodName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imageFoodView);
            }
        };

        foodAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(foodAdapter);
    }

    private void ShowAddFoodDialog() {


        final AlertDialog.Builder dialog = new  AlertDialog.Builder(this);
        dialog.setTitle("Add new Food");
        dialog.setMessage("Please fill full information");
        dialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        LayoutInflater inflater = LayoutInflater.from(this);
        View add_food = inflater.inflate(R.layout.add_new_food_layout,null);

        edtFoodName = add_food.findViewById(R.id.edtFoodName);
        edtFoodDescription = add_food.findViewById(R.id.edtFoodDescription);
        edtFoodPrice = add_food.findViewById(R.id.edtFoodPrice);
        edtFoodDiscount = add_food.findViewById(R.id.edtFoodDiscount);
        btnSelect = add_food.findViewById(R.id.btnFoodSelect);
        btnUpload = add_food.findViewById(R.id.btnFoodUpload);

        dialog.setView(add_food);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImage();
            }
        });
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (newFood != null) {
                    foods.push().setValue(newFood);
                    newFood = null; // to use this function again with updated option
                    Snackbar.make(rootLayout, "New Category " + newFood.getName() + "is added", Snackbar.LENGTH_LONG).show();
                }
            }
        });
        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void UploadImage() {
        if (imageUri != null) {
            if (edtFoodName.getText().toString().equals("")&&edtFoodDescription.getText().toString().equals("")&&edtFoodPrice.getText().toString().equals("")&&edtFoodDiscount.getText().toString().equals("")) {
                Toast.makeText(FoodList.this, "Please Fill Missing Required Information !!", Toast.LENGTH_SHORT).show();
            } else {

                final ProgressDialog mdialog = new ProgressDialog(this);
                mdialog.setMessage("UPLOADING...");
                mdialog.show();
                String imageName = UUID.randomUUID().toString();
                final StorageReference file = storageReference.child("images/" + imageName);

                file.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mdialog.dismiss();
                        Toast.makeText(FoodList.this, "Image Uploaded successfully", Toast.LENGTH_SHORT).show();
                        file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                newFood = new Food(edtFoodName.getText().toString(), uri.toString(),
                                          edtFoodDescription.getText().toString(),edtFoodPrice.getText().toString(),edtFoodDiscount.getText().toString(),categoryId);
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FoodList.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mdialog.setMessage("Uploaded " + progress + " %");
                    }
                });
            }

        } else
            Toast.makeText(this, "Please Select Image First", Toast.LENGTH_SHORT).show();



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2 && resultCode == RESULT_OK)
        {
               imageUri = data.getData();
               btnSelect.setText("SELECTED!!");
        }
    }

    private void selectImage() {

        Intent select = new Intent(Intent.ACTION_GET_CONTENT);
        select.setType("image/*");
        startActivityForResult(select,2);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE))
            UpdateFood(foodAdapter.getRef(item.getOrder()).getKey(),foodAdapter.getItem(item.getOrder()));
        else
            DeleteFood(foodAdapter.getRef(item.getOrder()).getKey());

        return super.onContextItemSelected(item);
    }

    private void DeleteFood(String key) {

        foods.child(key).removeValue();
        Toast.makeText(this, "Food Deleted successfully !!", Toast.LENGTH_SHORT).show();
    }

    private void UpdateFood(final String key, final Food item) {

        AlertDialog.Builder updateDialog = new AlertDialog.Builder(this);
        updateDialog.setTitle("Updated Dialog")
                .setIcon(R.drawable.ic_shopping_cart_black_24dp);

        LayoutInflater inflater = LayoutInflater.from(this);
        View updateFood = inflater.inflate(R.layout.add_new_food_layout,null);

        edtFoodName = updateFood.findViewById(R.id.edtFoodName);
        edtFoodDescription = updateFood.findViewById(R.id.edtFoodDescription);
        edtFoodPrice = updateFood.findViewById(R.id.edtFoodPrice);
        edtFoodDiscount = updateFood.findViewById(R.id.edtFoodDiscount);
        btnSelect = updateFood.findViewById(R.id.btnFoodSelect);
        btnUpload = updateFood.findViewById(R.id.btnFoodUpload);

        updateDialog.setView(updateFood);

        edtFoodName.setText(item.getName());
        edtFoodDescription.setText(item.getDescription());
        edtFoodPrice.setText(item.getPrice());
        edtFoodDiscount.setText(item.getDiscount());

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImage();
            }
        });


        updateDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                // when change Image only
                if(newFood!=null){
                    foods.child(key).setValue(newFood);
                }
                else {
                    item.setName(edtFoodName.getText().toString());
                    item.setDescription(edtFoodDescription.getText().toString());
                    item.setPrice(edtFoodPrice.getText().toString());
                    item.setDiscount(edtFoodDiscount.getText().toString());

                    foods.child(key).setValue(item);
                }
                Snackbar.make(rootLayout,item.getName()+" is updated",Snackbar.LENGTH_LONG).show();
            }
        });
        updateDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        updateDialog.show();

    }

    }

