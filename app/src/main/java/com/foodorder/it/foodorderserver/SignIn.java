package com.foodorder.it.foodorderserver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.foodorder.it.foodorderserver.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.widget.FButton;

public class SignIn extends AppCompatActivity {

    @BindView(R.id.edphone)
    MaterialEditText phone;
    @BindView(R.id.edpassword)
    MaterialEditText password;
    @BindView(R.id.btnSignIn)
    FButton signIn;

    FirebaseDatabase db;
    DatabaseReference user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        // Init firebase database
        db = FirebaseDatabase.getInstance();
        user = db.getReference("User");

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                mDialog.setMessage("Please waiting !....");
                mDialog.show();

                user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // check if the user not exist
                        if (dataSnapshot.child(phone.getText().toString()).exists()) {
                            mDialog.dismiss();
                            //get user information
                            User user = dataSnapshot.child(phone.getText().toString()).getValue(User.class);
                            user.setPhone(phone.getText().toString());

                            if (Boolean.parseBoolean(user.getIsStaff())) // if the user is staff
                            {
                                if (user.getPassword().equals(password.getText().toString())) {
                                    Intent homeIntent = new Intent(SignIn.this, Home.class);
                                    com.foodorder.it.foodorderserver.Common.Common.CurrentUser = user;
                                    startActivity(homeIntent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Wrong Password!!", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Please login with staff account!!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "User not exist in the database !!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
    }
}
