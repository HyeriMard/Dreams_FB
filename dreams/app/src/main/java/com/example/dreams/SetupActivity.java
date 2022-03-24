package com.example.dreams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 101;
    CircleImageView profileImageView;
    EditText inputUsername,inputCity,inputCountry,inputProfession;
    Button btnSave;
    Uri imageUri;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef;
    StorageReference storageRef;
    ProgressDialog mLoadingBar;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Setup Profile");
        profileImageView = findViewById(R.id.profile_image);
        inputUsername = findViewById(R.id.inputUserName);
        inputCity = findViewById(R.id.inputCity);
        inputCountry = findViewById(R.id.inputCountry);
        inputProfession = findViewById(R.id.inputProfession);
        btnSave = findViewById(R.id.btnSave);
        mLoadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("ProfileImage");
        //tới thư viện ảnh
        profileImageView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,REQUEST_CODE);
        });
        btnSave.setOnClickListener(view -> SaveData());
    }
    //lưu dữ liệu vào database
    private void SaveData(){
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
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mLoadingBar.setTitle("adding setup Profile");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            //Save profile
            storageRef.child(mUser.getUid()).putFile(imageUri).addOnCompleteListener(task -> {
                if(task.isSuccessful())
                {
                    storageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(uri -> {
                        HashMap hashMap = new HashMap();
                        String id =mUser.getUid();
                        hashMap.put("id",id);
                        hashMap.put("username",Username);
                        hashMap.put("city",City);
                        hashMap.put("country",Country);
                        hashMap.put("profession",Profession);
                        hashMap.put("profileImage",uri.toString());
                        hashMap.put("status","offline");
                        mRef.child(id).updateChildren(hashMap).addOnSuccessListener(o -> {
                            mLoadingBar.dismiss();
                            Toast.makeText(SetupActivity.this, "Setup Profile complete", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SetupActivity.this,MainActivity.class);
                            startActivity(intent);
                        }).addOnFailureListener(e -> {
                            mLoadingBar.dismiss();
                            Toast.makeText(SetupActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        });
                    });
                }
            });
        }
    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }
    //đổi ảnh thành uri
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data !=null)
        {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }
}