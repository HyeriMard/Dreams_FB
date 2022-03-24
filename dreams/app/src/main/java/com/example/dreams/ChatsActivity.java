package com.example.dreams;

import static com.example.dreams.String_Until.OtherUserID;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dreams.Model.Chats;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.drjacky.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class ChatsActivity extends AppCompatActivity {
    private String call = "";
    Toolbar toolbar;
    RecyclerView recyclerView;
    PhotoView secondImage, firstImage;
    EditText inputSms;
    ImageView btnSend, ImageVideoCall, imageView;
    CircleImageView userProfileImageAppbar;
    TextView usernameAppbar, status;
    DatabaseReference mRef, smRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    String OtherUsername, OtherUserProfileImageLink, OtherStatus;
    FirebaseRecyclerOptions<Chats> options;
    FirebaseRecyclerAdapter<Chats, ChatMyViewHolder> adapter;
    String myProfileImageLink, myUsername;
    String URL = "https://fcm.googleapis.com/fcm/send";
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//        OtherUserID = getIntent().getStringExtra("OtherUserID");
        requestQueue = Volley.newRequestQueue(this);
        recyclerView = findViewById(R.id.recyclerView);
        inputSms = findViewById(R.id.inputSms);
        btnSend = findViewById(R.id.btnSend);
        imageView = findViewById(R.id.imageView);
        ImageVideoCall = toolbar.findViewById(R.id.ImageVideoCall);
        userProfileImageAppbar = findViewById(R.id.userProfileImageAppbar);
        usernameAppbar = findViewById(R.id.usernameAppbar);
//        status = findViewById(R.id.status);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        smRef = FirebaseDatabase.getInstance().getReference().child("Message");
        String SECRET_KEY = "stackjava.com.if";
        SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        LoadOtherUser();
        LoadMyProfile();
        btnSend.setOnClickListener(view -> SendSMS());
        imageView.setOnClickListener(view -> {
            ImagePicker.Companion.with(this)
                    .crop()
                    .cropOval()
                    .maxResultSize(512, 512, true)
                    .createIntentFromDialog(new Function1() {
                        public Object invoke(Object var1) {
                            this.invoke((Intent) var1);
                            return Unit.INSTANCE;
                        }

                        public final void invoke(@NotNull Intent it) {
                            Intrinsics.checkNotNullParameter(it, "it");
                            launcher.launch(it);
                        }
                    });
        });
        LoadSMS();
        // gọi
        ImageVideoCall.setOnClickListener(view -> {
            HashMap hashMap = new HashMap();
            hashMap.put("sms", "callVideo");
            hashMap.put("status", "unseen");
            hashMap.put("userID", mUser.getUid());
            hashMap.put("type", "3");
            smRef.child(OtherUserID).child(mUser.getUid()).push().updateChildren(hashMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    smRef.child(mUser.getUid()).child(OtherUserID).push().updateChildren(hashMap).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            inputSms.setText(null);
                            Toast.makeText(ChatsActivity.this, "SMS call sent", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            Intent intent = new Intent(this, CallVideoActivity.class);
            intent.putExtra("OtherUserID", OtherUserID);
            startActivity(intent);
        });
        // trở về danh sách
        checkCall();
    }

    //Load ảnh của người dùng
    private void LoadMyProfile() {
        mRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    myProfileImageLink = snapshot.child("profileImage").getValue().toString();
                    myUsername = snapshot.child("username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatsActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
                if (result.getResultCode() == RESULT_OK) {
                    assert result.getData() != null;
                    Uri uri;
                    uri = result.getData().getData();
                    // Use the uri to load the image
                    if (uri == null) {
                        return;
                    } else {
                        try (FileInputStream os = (FileInputStream) getContentResolver().openInputStream(uri)) {
                            Bitmap image = BitmapFactory.decodeStream(os);
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            image.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                            String d = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
                            HashMap hashMap = new HashMap();
                            hashMap.put("sms", d);
                            hashMap.put("status", "unseen");
                            hashMap.put("userID", mUser.getUid());
                            hashMap.put("type", "2");
                            smRef.child(OtherUserID).child(mUser.getUid()).push().updateChildren(hashMap).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    smRef.child(mUser.getUid()).child(OtherUserID).push().updateChildren(hashMap).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            inputSms.setText(null);
                                            Toast.makeText(ChatsActivity.this, "Image has sent", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (result.getResultCode() == ImagePicker.RESULT_ERROR) {
                    ImagePicker.Companion.getError(result.getData());
                }
            });

    //Load tin nhắn
    private void LoadSMS() {
        options = new FirebaseRecyclerOptions.Builder<Chats>().setQuery(smRef.child(mUser.getUid()).child(OtherUserID), Chats.class).build();
        adapter = new FirebaseRecyclerAdapter<Chats, ChatMyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatMyViewHolder holder, int position, @NonNull Chats model) {
                if (model.getUserID().equals(mUser.getUid())) {
                    holder.fistUserText.setVisibility(View.GONE);
                    holder.fistUserProfile.setVisibility(View.GONE);
                    holder.firstChatCall.setVisibility(View.GONE);
                    holder.firstImage.setVisibility(View.GONE);
                    holder.secondUserProfile.setVisibility(View.VISIBLE);
                    if (model.getSms().equals("callVideo")) {
                        holder.secondUserText.setVisibility(View.GONE);
                        holder.secondChatCall.setVisibility(View.VISIBLE);
                        holder.secondImage.setVisibility(View.GONE);
                    } else if (model.getType().equals("2")) {
                        holder.secondImage.setVisibility(View.VISIBLE);
                        Bitmap bitmap = getBitmapFromString(model.getSms());
                        holder.secondImage.setImageBitmap(bitmap);
                        holder.secondUserText.setVisibility(View.GONE);
                        holder.secondChatCall.setVisibility(View.GONE);
                    } else {
                        holder.secondImage.setVisibility(View.GONE);
                        holder.secondChatCall.setVisibility(View.GONE);
                        holder.secondUserText.setVisibility(View.VISIBLE);
                        int k = 2;
                        String s = model.getSms();
                        int n = s.length();
                        int sd, sc;
                        sd = k;
                        sc = n / sd + 1;
                        int sokytu = sc;
                        int sodu = n % sd;
                        int t = 0;
                        char[][] hr = new char[sd][sc];
                        for (int i = 0; i < sd; i++) {
                            if (i >= sodu) {
                                sokytu = sc - 1;
                            }
                            for (int j = 0; j < sokytu; j++) {
                                hr[i][j] = s.charAt(t);
                                t++;
                            }
                        }
                        int c, d;
                        c = 0;
                        d = 0;
                        String kq = "";
                        for (int i = 0; i < n; i++) {
                            kq += hr[d][c];
                            d++;
                            if (d == k) {
                                c++;
                                d = 0;
                            }

                        }
                        holder.secondUserText.setText(kq);
                    }
                    Picasso.get().load(myProfileImageLink).into(holder.secondUserProfile);
                } else {
                    holder.fistUserProfile.setVisibility(View.VISIBLE);
                    holder.secondUserText.setVisibility(View.GONE);
                    holder.secondUserProfile.setVisibility(View.GONE);
                    holder.secondImage.setVisibility(View.GONE);
                    holder.secondChatCall.setVisibility(View.GONE);
                    if (model.getSms().equals("callVideo")) {
                        holder.firstImage.setVisibility(View.GONE);
                        holder.fistUserText.setVisibility(View.GONE);
                        holder.firstChatCall.setVisibility(View.VISIBLE);
                    } else if (model.getType().equals("2")) {
                        holder.firstImage.setVisibility(View.VISIBLE);
                        holder.fistUserText.setVisibility(View.GONE);
                        Bitmap bitmap = getBitmapFromString(model.getSms());
                        holder.firstImage.setImageBitmap(bitmap);
                        holder.firstChatCall.setVisibility(View.GONE);
                    } else {
                        holder.firstImage.setVisibility(View.GONE);
                        holder.fistUserText.setVisibility(View.VISIBLE);
                        holder.firstChatCall.setVisibility(View.GONE);
                        int k = 2;
                        String s = model.getSms();
                        int n = s.length();
                        int sd, sc;
                        sd = k;
                        sc = n / sd + 1;
                        int sokytu = sc;
                        int sodu = n % sd;
                        int t = 0;
                        char[][] hr = new char[sd][sc];
                        for (int i = 0; i < sd; i++) {
                            if (i >= sodu) {
                                sokytu = sc - 1;
                            }
                            for (int j = 0; j < sokytu; j++) {
                                hr[i][j] = s.charAt(t);
                                t++;
                            }
                        }
                        int c, d;
                        c = 0;
                        d = 0;
                        String kq = "";
                        for (int i = 0; i < n; i++) {
                            kq += hr[d][c];
                            d++;
                            if (d == k) {
                                c++;
                                d = 0;
                            }

                        }
                        holder.fistUserText.setText(kq);
                    }
                    Picasso.get().load(OtherUserProfileImageLink).into(holder.fistUserProfile);
                }
            }

            @NonNull
            @Override
            public ChatMyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.singleview_sms, parent, false);
                return new ChatMyViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    //Gửi tin nhắn
    private void SendSMS() {
        String sms = inputSms.getText().toString();
        if (sms.isEmpty()) {
            Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show();
        } else {
            int k = 2;
            int n = sms.length();
            int sd, sc;
            sd = k;
            sc = n / sd + 1;
            char[][] hr = new char[sd][sc];
            int c, d;
            c = 0;
            d = 0;
            int sodu = n % sd;
            for (int i = 0; i < n; i++) {
                hr[d][c] = sms.charAt(i);
                d++;
                if (d == k) {
                    c++;
                    d = 0;
                }
            }
            String kq = "";
            int sokytu = sc;
            for (int i = 0; i < sd; i++) {
                if (i > sodu) sokytu = sc - 1;
                for (int j = 0; j < sokytu; j++) {
                    kq = kq + hr[i][j];
                }
            }
            HashMap hashMap = new HashMap();
            hashMap.put("sms", kq);
            hashMap.put("status", "unseen");
            hashMap.put("userID", mUser.getUid());
            hashMap.put("type","1");
            smRef.child(OtherUserID).child(mUser.getUid()).push().updateChildren(hashMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    smRef.child(mUser.getUid()).child(OtherUserID).push().updateChildren(hashMap).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            sendNotification(sms);
                            inputSms.setText(null);
                            Toast.makeText(ChatsActivity.this, "SMS Sent", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    //Gửi  thong báo
    private void sendNotification(String sms) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", "/topics/" + OtherUserID);
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("title", "Message form  " + myUsername);
            jsonObject1.put("body", sms);
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("userID", mUser.getUid());
            jsonObject2.put("type", "sms");
            jsonObject.put("notification", jsonObject1);
            jsonObject.put("data", jsonObject2);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, jsonObject, response -> {

            }, error -> {

            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> map = new HashMap<>();
                    map.put("content-type", "application/json");
                    map.put("authorization", "key=AAAAVfielRM:APA91bGSQcpk-98lI2JuOjHfLPEI1yPQPgPXPyw2dzsHIA6WDrfr5yRo3CAduTOA4fyL7_SJkjBq0IWZkcIQtJsaKppUbpr-Cqvkn4Zq5VqleuGcGWsIIvT5RiQ_c-2cpL2cfIvh7801");
                    return map;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Load thông tin của người gửi
    private void LoadOtherUser() {
        mRef.child(OtherUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    OtherUsername = snapshot.child("username").getValue().toString();
                    OtherUserProfileImageLink = snapshot.child("profileImage").getValue().toString();
//                    OtherStatus = snapshot.child("status").getValue().toString();
                    Picasso.get().load(OtherUserProfileImageLink).into(userProfileImageAppbar);
                    usernameAppbar.setText(OtherUsername);
//                    status.setText(OtherStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatsActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap getBitmapFromString(String image) {
        byte[] bytes = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private String mahoa(String br, int k) {
        String kq = "";
        int n = br.length();
        for (int i = 0; i < n; i++)
            kq += mahoakt(br.charAt(i), k);
        return kq;
    }

    char mahoakt(char c, int k) {
        if (!Character.isLetter(c)) return c;
        return (char) ((((Character.toUpperCase(c) - 'A') + k) % 36 + 26) % 26 + 'A');
    }

    // kiểm tra cuộc gọi tới
    private void checkCall() {
        mRef.child(mUser.getUid()).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("ringing")) {
                    // nếu có trạng thái ringing thì chuyển qua activity gọi video
                    call = snapshot.child("ringing").getValue().toString();
                    Intent intent = new Intent(ChatsActivity.this, CallVideoActivity.class);
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
    public void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}