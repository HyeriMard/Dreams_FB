package com.example.dreams;

import static androidx.core.content.res.ResourcesCompat.getFont;
import static com.example.dreams.String_Until.RC_SIGN_IN;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kiprotich.japheth.TextAnim;

import java.util.Arrays;

import www.sanju.motiontoast.MotionToast;

public class Login extends AppCompatActivity {
    private TextAnim textWriter;
    EditText edtSDT, edtMK;
    CheckBox ckbHienMK, ckbLuuTT;
    Button btnDangNhap;
    ImageButton ibtnGG, ibtnFB;
    //LoginButton ibtnFB;
    TextView txtDangKy, txtressetpass;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "firebase - LOGIN";
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        RegisterLogo();
        mAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
        edtSDT = findViewById(R.id.edtSDT);
        edtMK = findViewById(R.id.edtMK);
        ckbHienMK = findViewById(R.id.ckbHienMK);
        ckbLuuTT = findViewById(R.id.ckbLuuTT);
        btnDangNhap = findViewById(R.id.btnDangNhap);
        ibtnGG = findViewById(R.id.ibtnGG);
        ibtnFB = findViewById(R.id.ibtnFB);
        txtDangKy = findViewById(R.id.txtDangKy);
        txtressetpass = findViewById(R.id.txtresetpass);
        createRequest();
        loadData();
        txtDangKy.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, RegistrationActivity.class);
            startActivity(intent);
        });
        txtressetpass.setOnClickListener(view -> CustomSendMail());
        btnDangNhap.setOnClickListener(view -> {
            if (ckbLuuTT.isChecked()) {
                saveData(edtSDT.getText().toString(), edtMK.getText().toString());
            } else {
                clearData();
            }
            String userNameInput = edtSDT.getText().toString().trim();
            String passwordInput = edtMK.getText().toString().trim();

            Boolean checkError = true;
            if (userNameInput.isEmpty()) {
                edtSDT.setError("Nhập tên đăng nhập");
                checkError = false;
            }
            if (passwordInput.isEmpty()) {
                edtMK.setError("Nhập mật khẩu");
                checkError = false;
            }
            if (passwordInput.length() < 6) {
                edtMK.setError("Mật khẩu ít nhất 6 kí tự");
                checkError = false;
            }
            if (checkError) {
                loginUser(userNameInput, passwordInput);
            }
        });
        ibtnGG.setOnClickListener(view -> signIn());
        //Hiện mật khẩu
        ckbHienMK.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                edtMK.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                edtMK.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        //dawng nhap bang faceboook
        //ibtnFB.setReadPermissions("email", "public_profile");
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(getApplicationContext(), "facebook:onSuccess:" + loginResult, Toast.LENGTH_SHORT).show();
                        handleFacebookAccessToken(loginResult.getAccessToken());

                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "facebook:onCancel", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(getApplicationContext(), "facebook:onError", Toast.LENGTH_SHORT).show();
                    }
                });
        ibtnFB.setOnClickListener(view -> LoginManager.getInstance().logInWithReadPermissions(Login.this, Arrays.asList("public_profile", "user_friends")));
    }

    //Lưu đăng nhập
    private void saveData(String username, String Pass) {
        SharedPreferences preferences1 = getSharedPreferences("luutaikhoan", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences1.edit();
        editor.putString("USERNAME", username);
        editor.putString("PASS", Pass);
        editor.putBoolean("nho", ckbLuuTT.isChecked());
        editor.commit();
    }

    private void clearData() {
        SharedPreferences preferences1 = getSharedPreferences("luutaikhoan", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences1.edit();
        editor.clear();
        editor.commit();
    }

    private void loadData() {
        SharedPreferences preferences1 = getSharedPreferences("luutaikhoan", MODE_PRIVATE);
        if (preferences1.getBoolean("nho", false)) {
            edtSDT.setText(preferences1.getString("USERNAME", ""));
            edtMK.setText(preferences1.getString("PASS", ""));
            ckbLuuTT.setChecked(true);
        } else
            ckbLuuTT.setChecked(false);

    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Login unsuccessful", task.getException());
                        Toast.makeText(Login.this, "Login unsuccessful",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Login.this, "Logged in successfully",
                                Toast.LENGTH_SHORT).show();
                        mUser = FirebaseAuth.getInstance().getCurrentUser();
                        String Id = mUser.getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        Query checkUser = ref.orderByChild("id").equalTo(Id);
                        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String username = snapshot.child(Id).child("username").getValue(String.class);
                                if (username != null) {
                                    startActivity(new Intent(Login.this, MainActivity.class));
                                } else {
                                    startActivity(new Intent(Login.this, SetupActivity.class));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
    }

    //đăng nhập bằng google
    public void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //token google
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "signInWithCredential:success", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(Login.this, SetupActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "signInWithCredential:failure", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //token facebook
    private void handleFacebookAccessToken(AccessToken token) {
        Toast.makeText(getApplicationContext(), "handleFacebookAccessToken:" + token, Toast.LENGTH_SHORT).show();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Toast.makeText(getApplicationContext(), "signInWithCredential:success", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //quên pass word
    public void Resetpass(String email) {
        if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Vui lòng nhập email đã đăng ký !!!", Toast.LENGTH_SHORT).show();
        }
        //if (email.equals(user.getEmail())){
        //   Toast.makeText(getApplicationContext(), "Email không đúng !!! Vui lòng nhập email đăng ký !!!", Toast.LENGTH_SHORT).show();
        //}
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                MotionToast.Companion.darkToast(this,
                        "Save image",
                        "Send code success, pls login again!",
                        MotionToast.TOAST_SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        getFont(this, R.font.helvetica_regular));
//                Toast.makeText(Login.this, "Đã gửi mã thành công vui lòng đăng nhập để đổi mật khẩu!", Toast.LENGTH_SHORT).show();
            } else {
                MotionToast.Companion.darkToast(this,
                        "Error 404",
                        "Send code failed, try again",
                        MotionToast.TOAST_ERROR,
                        MotionToast.GRAVITY_CENTER,
                        MotionToast.LONG_DURATION,
                        getFont(this, R.font.helvetica_regular));
//                Toast.makeText(Login.this, "Gửi mã reset thất bại !!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //gui mail reset pass
    public void CustomSendMail() {
        AppCompatButton btntieptuc, btnhuy1, btnguima;
        EditText edtmaisend, edtpassresset;
        AlertDialog.Builder daigogBuilder = new AlertDialog.Builder(Login.this);
        LayoutInflater inflater = Login.this.getLayoutInflater();
        View dailogView = inflater.inflate(R.layout.layout_senmail, null);
        btnguima = dailogView.findViewById(R.id.btnguima);
        btnhuy1 = dailogView.findViewById(R.id.btnhuy1);
        edtmaisend = dailogView.findViewById(R.id.edtmailsend);
        daigogBuilder.setView(dailogView);
        AlertDialog b = daigogBuilder.create();
        b.show();
        btnguima.setOnClickListener(view -> {
            Resetpass(edtmaisend.getText().toString().trim());
            b.cancel();
        });
        btnhuy1.setOnClickListener(view -> b.cancel());
    }

    private void RegisterLogo() {
        textWriter = findViewById(R.id.textWriter);
        textWriter
                .setWidth(15)
                .setDelay(30)
                .setColor(Color.BLACK)
                .setConfig(TextAnim.Configuration.INTERMEDIATE)
                .setSizeFactor(35f)
                .setLetterSpacing(25f)
                .setText("LOGIN")
                .startAnimation();
    }
}