package com.example.dreams;

import static com.example.dreams.String_Until.OtherUserID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewFriendActivity extends AppCompatActivity {
    DatabaseReference mUserRef,requestRef,friendRef;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String profileImageUrl,username,city,country;
    CircleImageView profileImage;
    TextView Username,address;
    AppCompatButton btnDecline,btnPerform;
    String CurrentState = "nothing_happen";
    String profession;
    String userID;
    String myProfileImageUrl,mUsername,mCity,mProfession,mCountry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friend);
        userID = getIntent().getStringExtra("userKey");
        Toast.makeText(this, ""+userID, Toast.LENGTH_SHORT).show();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
       requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        profileImage = findViewById(R.id.profileImage);
        Username = findViewById(R.id.username);
        address = findViewById(R.id.address);
        btnDecline = findViewById(R.id.btnDecline);
        btnPerform = findViewById(R.id.btnPreform);
        LoadUser();
        LoadMyProFile();
        btnPerform.setOnClickListener(view -> PerformAction(userID));
        CheckUserExistance(userID);
        btnDecline.setOnClickListener(view -> UnFriend(userID));
    }

    private void LoadMyProFile() {
        mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    myProfileImageUrl = snapshot.child("profileImage").getValue().toString();
                    mUsername = snapshot.child("username").getValue().toString();
                    mCity = snapshot.child("city").getValue().toString();
                    mCountry = snapshot.child("country").getValue().toString();
                    mProfession = snapshot.child("profession").getValue().toString();
                }
                else
                {
                    Toast.makeText(ViewFriendActivity.this, "Data not fount", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Hủy Kết bạn
    private void UnFriend(String userID) {
        if(CurrentState.equals("friend")){
            friendRef.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful())
                {
                    friendRef.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful())
                        {
                            Toast.makeText(ViewFriendActivity.this, "You are Unfriend", Toast.LENGTH_SHORT).show();
                            CurrentState ="nothing_happen";
                            btnPerform.setText("Sent Friend Request");
                            btnDecline.setVisibility(View.GONE);
                        }
                    });
                }
            });
        }
        if(CurrentState.equals("he_send_pending"))
        {
            HashMap hashMap = new HashMap();
            hashMap.put("status","decline");
            requestRef.child(userID).child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(task -> {
                if(task.isSuccessful())
                {
                    Toast.makeText(ViewFriendActivity.this, "You have Decline Friend", Toast.LENGTH_SHORT).show();
                    CurrentState ="he_sent_decline";
                    btnPerform.setVisibility(View.GONE);
                    btnDecline.setVisibility(View.GONE);
                }
            });
        }
    }

   //Xác nhận kết bạn
    private void CheckUserExistance(String userID) {
        friendRef.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    CurrentState = "friend";
                    btnPerform.setText("Send SMS");
                    btnDecline.setText("UnFiend");
                    btnDecline.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        friendRef.child(userID).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    CurrentState = "friend";
                    btnPerform.setText("Send SMS");
                    btnDecline.setText("UnFiend");
                    btnDecline.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        requestRef.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("status").getValue().toString().equals("pending")){
                        CurrentState ="I_sent_pending";
                        btnPerform.setText("Cancel Friend Request");
                        btnDecline.setVisibility(View.GONE);
                    }
                }
                if(snapshot.exists()){
                    if(snapshot.child("status").getValue().toString().equals("decline")){
                        CurrentState ="I_sent_decline";
                        btnPerform.setText("Cancel Friend Request");
                        btnDecline.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        requestRef.child(userID).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    if(snapshot.child("status").getValue().toString().equals("pending"))
                    {
                        CurrentState = "he_sent_pending";
                        btnPerform.setText("Accept Friend Request");
                        btnDecline.setText("Decline Friend");
                        btnDecline.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(CurrentState.equals("nothing_happen"))
        {
            CurrentState="nothing_happen";
            btnPerform.setText("Send Friend Request");
            btnDecline.setVisibility(View.GONE);
        }
    }
    //Gửi lời mời kết bạn
    private void PerformAction(String userID) {
        if(CurrentState.equals("nothing_happen"))
        {
            HashMap hashMap = new HashMap();
            hashMap.put("status","pending");
            requestRef.child(mUser.getUid()).child(userID).updateChildren(hashMap).addOnCompleteListener(task -> {
                if(task.isSuccessful())
                {
                    Toast.makeText(ViewFriendActivity.this, "You Have Sent Friend Request", Toast.LENGTH_SHORT).show();
                    btnDecline.setVisibility(View.GONE);
                    CurrentState = "I_sent_pending";
                    btnPerform.setText("Cancel Friend Request");
                }
                else
                {
                    Toast.makeText(ViewFriendActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        if(CurrentState.equals("I_sent_pending")||CurrentState.equals("I_sent_decline"))
        {
            requestRef.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful())  {
                    Toast.makeText(ViewFriendActivity.this, "You Have Cancelled Friend Request", Toast.LENGTH_SHORT).show();
                    btnDecline.setVisibility(View.GONE);
                    CurrentState = "nothing_happen";
                    btnPerform.setText("Send Friend Request");
                }
                else
                {
                    Toast.makeText(ViewFriendActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        if(CurrentState.equals("he_sent_pending"))
        {
            requestRef.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful())
                {
                    HashMap hashMap = new HashMap();
                    hashMap.put("status","friend");
                    hashMap.put("username",username);
                    hashMap.put("profileImageUrl",profileImageUrl);
                    hashMap.put("profession",profession);
                    final HashMap hashMap1 = new HashMap();
                    hashMap1.put("status","friend");
                    hashMap1.put("username",mUsername);
                    hashMap1.put("profileImageUrl",myProfileImageUrl);
                    hashMap1.put("profession",mProfession);
                    friendRef.child(mUser.getUid()).child(userID).updateChildren(hashMap).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful())
                        {
                            friendRef.child(userID).child(mUser.getUid()).updateChildren(hashMap1).addOnCompleteListener(task11 -> {
                                Toast.makeText(ViewFriendActivity.this, "You added friend", Toast.LENGTH_SHORT).show();
                                CurrentState="friend";
                                btnPerform.setText("Send SMS");
                                btnDecline.setText("UnFiend");
                                btnDecline.setVisibility(View.VISIBLE);
                            });
                        }
                    });
                }
            });
        }
        if(CurrentState.equals("friend"))
        {
            Intent intent = new Intent(ViewFriendActivity.this,ChatsActivity.class);
            OtherUserID = userID;
            intent.putExtra("OtherUserID",OtherUserID);
            startActivity(intent);
        }
    }
//    //Load thông tin user
    private void LoadUser() {
        mUserRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    username = snapshot.child("username").getValue().toString();
                    city = snapshot.child("city").getValue().toString();
                    country = snapshot.child("country").getValue().toString();
                    profession = snapshot.child("profession").getValue().toString();
                    Picasso.get().load(profileImageUrl).into(profileImage);
                    Username.setText(username);
                    address.setText(city+"-"+country);
                }
                else
                {
                    Toast.makeText(ViewFriendActivity.this, "Data no found", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewFriendActivity.this, ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }
    @Override
    public void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}