package com.example.dreams;

import static com.example.dreams.String_Until.OtherUserID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class CallVideoActivity extends AppCompatActivity {
    LottieAnimationView accept_call, cancel_call;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    ImageView profile_image_calling;
    DatabaseReference mRef;
    String ProfileImageLink, Username;
    String myProfileImageLink, myUsername, myID, checker = "";
    private String callingID = "", ringingID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_video);
        cancel_call = findViewById(R.id.cancel_call);
        accept_call = findViewById(R.id.make_call);
        myID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        profile_image_calling = findViewById(R.id.profile_image_calling);
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        cancel_call.setOnClickListener(view -> {
            checker = "clicked";
            cancelCall();
        });
        // chấp nhận cuộc gọi chuyển wa activity phát video call
        accept_call.setOnClickListener(view -> {
            final HashMap<String, Object> callingPickUpMap = new HashMap<>();
            callingPickUpMap.put("picked", "picked");
            mRef.child(myID).child("Ringing")
                    .updateChildren(callingPickUpMap)
                    .addOnCompleteListener(task -> {
                        Intent intent = new Intent(CallVideoActivity.this, VideoCallActivity.class);
                        startActivity(intent);
                        finish();
                    });
        });
        LoadOtherUser();
    }

    //Load ảnh người được gọi trên màn
    private void LoadOtherUser() {
        mRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(OtherUserID).exists()) {
                    Username = snapshot.child(OtherUserID).child("username").getValue().toString();
                    ProfileImageLink = snapshot.child(OtherUserID).child("profileImage").getValue().toString();
                    Picasso.get().load(ProfileImageLink).into(profile_image_calling);
                }
                if (snapshot.child(myID).exists()) {
                    myProfileImageLink = snapshot.child(myID).child("profileImage").getValue().toString();
                    myUsername = snapshot.child(myID).child("username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // khi bắt đầu trước khi được khởi tạo
    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
        mRef.child(OtherUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!checker.equals("clicked") && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing")) {
                    final HashMap<String, Object> call = new HashMap<>();
                    call.put("calling", OtherUserID);
                    mRef.child(myID).child("Calling").updateChildren(call).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            final HashMap<String, Object> ring = new HashMap<>();
                            ring.put("ringing", myID);
                            mRef.child(OtherUserID).child("Ringing").updateChildren(ring);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(myID).hasChild("Ringing") && !snapshot.child(myID).hasChild("Calling")) {
                    accept_call.setVisibility(View.VISIBLE);
                }
                if (snapshot.child(OtherUserID).child("Ringing").hasChild("picked")) {
                    Intent intent = new Intent(CallVideoActivity.this, VideoCallActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Không chấp nhận cuộc gọi được gửi tới
    private void cancelCall() {
        // người gửi cuộc gọi
        mRef.child(myID).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("Calling")) {
                    callingID = snapshot.child("calling").getValue().toString();
                    mRef.child(callingID).child("Ringing").removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mRef.child(myID).child("Calling").removeValue().addOnCompleteListener(task12 -> {
                                startActivity(new Intent(CallVideoActivity.this, ChatsActivity.class));
                                finish();
                            });
                        }
                    });
                } else {
                    startActivity(new Intent(CallVideoActivity.this, ChatsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // người nhận cuộc gọi
        mRef.child(myID).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("ringing")) {
                    ringingID = snapshot.child("ringing").getValue().toString();
                    mRef.child(ringingID).child("Calling").removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mRef.child(myID).child("Ringing").removeValue().addOnCompleteListener(task1 -> {
                                Intent intent = new Intent(CallVideoActivity.this, ChatsActivity.class);
                                intent.putExtra("OtherUserID", OtherUserID);
                                startActivity(intent);
                                finish();
                            });
                        }
                    });
                } else {
                    startActivity(new Intent(CallVideoActivity.this, ChatsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}