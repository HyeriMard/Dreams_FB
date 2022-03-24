package com.example.dreams;

import static com.example.dreams.String_Until.OtherUserID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.dreams.Model.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendActivity extends AppCompatActivity {
    private String call = "";
    Toolbar toolbar;
    RecyclerView recyclerView;
    FirebaseRecyclerOptions<Friends> options;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    FirebaseRecyclerAdapter<Friends, FriendViewHolder> adapter;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        LoadFriends("");
        checkCall();
    }

    private void LoadFriends(String s) {
        Query query = mRef.child(mUser.getUid()).orderByChild("username").startAt(s).endAt(s + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();
        adapter = new FirebaseRecyclerAdapter<Friends, FriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendViewHolder holder, @SuppressLint("RecyclerView") final int position, @NonNull Friends model) {
                Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImageUrl);
                holder.username.setText(model.getUsername());
                holder.profession.setText(model.getProfession());
                holder.itemView.setOnClickListener(view -> {
                    OtherUserID = getRef(position).getKey();
                    dialog = new Dialog(FriendActivity.this);
                    dialog.setContentView(R.layout.user_infomation);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);
                    ConstraintLayout b = dialog.findViewById(R.id.UserIS);
                    CircleImageView logo = b.findViewById(R.id.logo);
                    ImageButton SendMess = b.findViewById(R.id.SendMess);
                    ImageButton CallVideo = b.findViewById(R.id.CallVideo);
                    TextView NameInfo = b.findViewById(R.id.NameInfo);
                    TextView nghenghiep = b.findViewById(R.id.nghenghiep);
                    Picasso.get().load(model.getProfileImageUrl()).into(logo);
                    NameInfo.setText(model.getUsername());
                    nghenghiep.setText(model.getProfession());
                    SendMess.setOnClickListener(view1 -> {
                        Intent intent = new Intent(FriendActivity.this, ChatsActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    });
                    CallVideo.setOnClickListener(view1 -> {
                        Intent intent = new Intent(FriendActivity.this, CallVideoActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    });
                    dialog.show();
                });
            }

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_friend, parent, false);
                return new FriendViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void checkCall() {
        mRef.child(mUser.getUid()).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("ringing")) {
                    // nếu có trạng thái ringing thì chuyển qua activity gọi video
                    call = snapshot.child("ringing").getValue().toString();
                    Intent intent = new Intent(FriendActivity.this, CallVideoActivity.class);
                    intent.putExtra("OtherUserID", call);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}