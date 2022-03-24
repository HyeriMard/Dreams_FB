package com.example.dreams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    CircleImageView profileImageView;
    EditText inputUsername,inputCity,inputCountry,inputProfession;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    Button btnSave,btnCancel;
    DatabaseReference mUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    StorageReference storageRef;
    Uri imageUri;
    ProgressDialog mLoadingBar;
    Toolbar toolbar;
    String profileImageUrl,city,country,profession,username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        profileImageView = findViewById(R.id.profile_image3);
        inputUsername = findViewById(R.id.inputUserName3);
        inputCity = findViewById(R.id.inputCity3);
        inputCountry = findViewById(R.id.inputCountry3);
        inputProfession = findViewById(R.id.inputProfession3);
        btnSave = findViewById(R.id.btnSave1);
        mLoadingBar = new ProgressDialog(this);
        btnCancel = findViewById(R.id.btnCancel);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("ProfileImage");
        //load và gán dữ liệu vào edit text
        mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    city = snapshot.child("city").getValue().toString();
                    country = snapshot.child("country").getValue().toString();
                    profession = snapshot.child("profession").getValue().toString();
                    username = snapshot.child("username").getValue().toString();
                    Picasso.get().load(profileImageUrl).into(profileImageView);
                    inputCity.setText(city);
                    inputCountry.setText(country);
                    inputProfession.setText(profession);
                    inputUsername.setText(username);
                }
                else
                {
                    Toast.makeText(ProfileActivity.this, "Data not exits", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, ""+error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this,MainActivity.class));
            finish();
        });
        btnSave.setOnClickListener(view -> {
            String Username = inputUsername.getText().toString();
            String City = inputCity.getText().toString();
            String Country = inputCountry.getText().toString();
            String Profession = inputProfession.getText().toString();

            if(Username.isEmpty() || Username.length()<3)
            {
                showError(inputUsername,"Username is not valid");
            }
            else if(City.isEmpty() || City.length() < 3)
            {
                showError(inputCity,"City is not valid");
            }
            else if(Country.isEmpty() || Country.length() < 3)
            {
                showError(inputCountry,"Country is not valid");
            }
            else if(Profession.isEmpty() || Profession.length() < 3)
            {
                showError(inputProfession,"Profession is not valid");
            }
            else if (imageUri == null)
            {
                //Toast.makeText(ProfileActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                mLoadingBar.setTitle("adding setup Profile");
                mLoadingBar.setCanceledOnTouchOutside(false);
                mLoadingBar.show();
                //Save profile
                storageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(uri -> {
                    HashMap hashMap = new HashMap();
                    String id =mUser.getUid();
                    hashMap.put("id",id);
                    hashMap.put("username",Username);
                    hashMap.put("city",City);
                    hashMap.put("country",Country);
                    hashMap.put("profession",Profession);
                    //hashMap.put("profileImage",uri.toString());
                    hashMap.put("status","offline");
                    mUserRef.child(id).updateChildren(hashMap).addOnSuccessListener(o -> {
                        mLoadingBar.dismiss();
                        Toast.makeText(ProfileActivity.this, "Setup Profile complete", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
                        startActivity(intent);
                    }).addOnFailureListener(e -> {
                        mLoadingBar.dismiss();
                        Toast.makeText(ProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });
    }
    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }
    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}