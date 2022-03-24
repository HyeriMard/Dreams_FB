package com.example.dreams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dreams.Model.Comments;
import com.example.dreams.Model.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.shreyaspatil.MaterialDialog.MaterialDialog;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_CODE = 101;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference MuserRef, PostRef, LikeRef, commentRef;
    String profileImageURLV, usernameV, mImg;
    CircleImageView profileImageHeader, myImage;
    TextView usernameHeader;
    ImageView addImagePost, sendImagePost;
    EditText inputAddPost;
    Uri imageUri;
    ProgressDialog mLoadingBar;
    StorageReference postImageRef;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    FirebaseRecyclerAdapter<Post, MyViewHolder> adapter;
    FirebaseRecyclerOptions<Post> options;
    RecyclerView recyclerView;
    FirebaseRecyclerOptions<Comments> CommentOptions;
    FirebaseRecyclerAdapter<Comments, CommentViewHolder> CommentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Strong Wall");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
        addImagePost = findViewById(R.id.add_imagepost);
        sendImagePost = findViewById(R.id.send_post_image);
        inputAddPost = findViewById(R.id.inputAddPost);
        mLoadingBar = new ProgressDialog(this);
        recyclerView = findViewById(R.id.rclhome);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myImage = findViewById(R.id.myImage);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        MuserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        commentRef = FirebaseDatabase.getInstance().getReference().child("Comments");
        postImageRef = FirebaseStorage.getInstance().getReference().child("PostImages");
        FirebaseMessaging.getInstance().subscribeToTopic(mUser.getUid());
        Getprofile();
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        View view = navigationView.inflateHeaderView(R.layout.drawer_header);
        usernameHeader = view.findViewById(R.id.username_header);
        profileImageHeader = view.findViewById(R.id.profileImage_header);
        navigationView.setNavigationItemSelectedListener(this);
        sendImagePost.setOnClickListener(view1 -> AddPost());
        //tới thư viện ảnh
        addImagePost.setOnClickListener(view12 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE);
        });
        LoadPost();
    }

    //load du lieu post
    private void LoadPost() {
        options = new FirebaseRecyclerOptions.Builder<Post>().setQuery(PostRef, Post.class).build();
        adapter = new FirebaseRecyclerAdapter<Post, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Post model) {
                String postkey = getRef(position).getKey();
                holder.txtpostDesc.setText(model.getPostDesc());
                String timeAgo = calculateIimeAgo(model.getDatePost());
                holder.txttimeago.setText(timeAgo);
                holder.txtusername.setText(model.getUsername());
                Picasso.get().load(model.getPostImageUrl()).into(holder.postImage);
                Picasso.get().load(model.getUserProfileImageUrl()).into(holder.profileImage);
                if(!model.getId().equalsIgnoreCase(mUser.getUid())){
                    holder.btndelete.setVisibility(View.GONE);
                }
                holder.btndelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Button btnCancel,btnDelete;
                        AlertDialog.Builder dialogBiuder = new AlertDialog.Builder(MainActivity.this);
                        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.layout_delete,null);
                        btnCancel = dialogView.findViewById(R.id.cancel);
                        btnDelete = dialogView.findViewById(R.id.Delete);
                        dialogBiuder.setView(dialogView);
                        dialogBiuder.setTitle("Delete Post");
                        AlertDialog b = dialogBiuder.create();
                        b.show();

                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                b.cancel();
                            }
                        });
                        btnDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PostRef.child(postkey).removeValue();
                                commentRef.child(postkey).removeValue();
                                b.cancel();
                            }
                        });
                    }
                });
                //Lưu và load like
                holder.countlike(postkey, mUser.getUid(), LikeRef);
                holder.countComment(postkey, mUser.getUid(), commentRef);
                holder.likeImage.setOnClickListener(view -> LikeRef.child(postkey).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            LikeRef.child(postkey).child(mUser.getUid()).removeValue();
                            holder.likeImage.setColorFilter(Color.GRAY);
                            notifyDataSetChanged();
                        } else {
                            LikeRef.child(postkey).child(mUser.getUid()).setValue("like");
                            holder.likeImage.setColorFilter(Color.GREEN);
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
                holder.commentSend.setOnClickListener(view -> {
                    String comment = holder.inputComment.getText().toString();
                    if (comment.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please write something in EditText", Toast.LENGTH_SHORT).show();
                    } else {
                        AddComment(holder, postkey, commentRef, mUser.getUid(), comment);
                    }
                });
                LoadCommet(postkey);
                holder.postImage.setOnClickListener(view -> {
                    Intent intent = new Intent(MainActivity.this, ImageViewActivity.class);
                    intent.putExtra("url", model.getPostImageUrl());
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_post, parent, false);
                return new MyViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    //Load Comment
    private void LoadCommet(String postkey) {
        MyViewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        CommentOptions = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(commentRef.child(postkey), Comments.class).build();
        CommentAdapter = new FirebaseRecyclerAdapter<Comments, CommentViewHolder>(CommentOptions) {
            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comments model) {
                Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImage);
                holder.username.setText(model.getUsername());
                holder.comment.setText(model.getComment());
            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_commet, parent, false);
                return new CommentViewHolder(view);
            }
        };
        CommentAdapter.startListening();
        MyViewHolder.recyclerView.setAdapter(CommentAdapter);
    }

    //Luu Comment
    private void AddComment(MyViewHolder holder, String postkey, DatabaseReference commentRef, String uid, String comment) {
        HashMap hashMap = new HashMap();
        hashMap.put("username", usernameV);
        hashMap.put("profileImageUrl", profileImageURLV);
        hashMap.put("comment", comment);
        commentRef.child(postkey).child(uid).updateChildren(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Comments Added", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                holder.inputComment.setText(null);
            } else {
                Toast.makeText(MainActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //đổi thời gian thành cách đây mấy phút
    private String calculateIimeAgo(String datePost) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            long time = sdf.parse(datePost).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return ago + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    //đổi ảnh thành uri
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            addImagePost.setImageURI(imageUri);
        }
    }

    //Lưu post user vào database
    private void AddPost() {
        String postDesc = inputAddPost.getText().toString();
        if (postDesc.isEmpty() || postDesc.length() < 3) {
            inputAddPost.setError("Please write something in post Desc ");
        } else if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        } else {
            mLoadingBar.setTitle("Adding Post");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            Date date = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            String strDate = formatter.format(date);
            postImageRef.child(mUser.getUid() + strDate).putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    postImageRef.child(mUser.getUid() + strDate).getDownloadUrl().addOnSuccessListener(uri -> {
                        HashMap hashMap = new HashMap();
                        hashMap.put("id",mUser.getUid());
                        hashMap.put("datePost", strDate);
                        hashMap.put("postImageUrl", uri.toString());
                        hashMap.put("postDesc", postDesc);
                        hashMap.put("userProfileImageUrl", profileImageURLV);
                        hashMap.put("username", usernameV);
                        PostRef.child(mUser.getUid() + strDate).updateChildren(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                mLoadingBar.dismiss();
                                Toast.makeText(MainActivity.this, "Post Added", Toast.LENGTH_SHORT).show();
                                addImagePost.setImageResource(R.drawable.ic_add_postimage);
                                inputAddPost.setText("");
                            } else {
                                mLoadingBar.dismiss();
                                Toast.makeText(MainActivity.this, "" + task1.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } else {
                    mLoadingBar.dismiss();
                    Toast.makeText(MainActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //Lấy profile
    public void Getprofile() {
        MuserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profileImageURLV = snapshot.child("profileImage").getValue().toString();
                    usernameV = snapshot.child("username").getValue().toString();
                    Picasso.get().load(profileImageURLV).into(profileImageHeader);
                    usernameHeader.setText(usernameV);
                    mImg = snapshot.child("profileImage").getValue().toString();
                    Picasso.get().load(mImg).into(myImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //click item menu
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case R.id.friend:
                startActivity(new Intent(MainActivity.this, FriendActivity.class));
                break;
            case R.id.findFriend:
                startActivity(new Intent(MainActivity.this, FindFriendActivity.class));
                break;
            case R.id.chat:
                startActivity(new Intent(MainActivity.this, ChatUserActivity.class));
                break;
            case R.id.inforapp:
                Context context = getApplicationContext();
                PackageManager packageManager = context.getPackageManager();
                String packageName = context.getPackageName();
                String myVersionName = "not available";
                try {
                    myVersionName = packageManager.getPackageInfo(packageName, 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                MaterialDialog mDialog = new MaterialDialog.Builder(this)
                        .setAnimation(R.raw.bubel_chat)
                        .setTitle("Alo chat")
                        .setMessage("Version : " + myVersionName)
                        .setCancelable(false)
                        .setPositiveButton("Ok", R.drawable.like, (dialogInterface, which) -> dialogInterface.dismiss())
                        .build();
                mDialog.show();
                break;
            case R.id.feedback:
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("strorebeemo@gmail.com") + "?subject="
                        + Uri.encode("Feedback") + Uri.encode("");
                Uri uri = Uri.parse(uriText);
                intent.setData(uri);
                startActivity(Intent.createChooser(intent, "send mail"));
                break;
            case R.id.logout:
                MaterialDialog log = new MaterialDialog.Builder(this)
                        .setAnimation(R.raw.log_out)
                        .setTitle("You want to exit !!")
                        .setMessage("Are you sure about that !")
                        .setCancelable(false)
                        .setPositiveButton("Yes", R.drawable.ic_baseline_exit_to_app_24, (dialogInterface, which) -> {
                            mAuth.signOut();
                            startActivity(new Intent(MainActivity.this, Login.class));
                            finish();
                        })
                        .setNegativeButton("No", R.drawable.ic_baseline_close_24, (dialogInterface, which) -> dialogInterface.dismiss())
                        .build();
                log.show();
                break;
        }
        return true;
    }

    //click menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return true;
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