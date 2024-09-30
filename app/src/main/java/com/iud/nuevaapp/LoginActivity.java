package com.iud.nuevaapp;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.DeviceLoginManager;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.Arrays;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_ING = 20;

    private EditText loginEmail, loginPassword;
    private TextView registrateAqui;
    private Button loginBtn;
    private Button googleBtn;
    private TextView forgotPassword;

    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        registrateAqui = findViewById(R.id.registrateAqui);
        loginEmail = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.btnLogin);
        googleBtn = findViewById(R.id.btnGoogle);
        forgotPassword = findViewById(R.id.forgotPassword);

        // Redireccionar hacia el register
        registrateAqui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this , RegisterActivity.class));
            }
        });

        // Autenticación por email y contraseña
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();
                if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if(!password.isEmpty()) {
                        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(LoginActivity.this, "Ingreso exitoso", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Ingreso fallido", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        loginPassword.setError("La contraseña no puede estar vacía");
                    }
                } else if(email.isEmpty()) {
                    loginEmail.setError("El email no puede estar vacío");
                } else {
                    loginEmail.setError("Ingrese un email valido");
                }
            }
        });

        // Autenticación con google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

        // Recuperar contraseña
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
                EditText emailBox = dialogView.findViewById(R.id.emailBox);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String userEmail = emailBox.getText().toString();
                        if(TextUtils.isEmpty(userEmail) && !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                            Toast.makeText(LoginActivity.this, "Ingresa tu correo registrado", Toast.LENGTH_SHORT).show();
                            return;
                        }
                         auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                             @Override
                             public void onComplete(@NonNull Task<Void> task) {
                                 if(task.isSuccessful()) {
                                     Toast.makeText(LoginActivity.this, "Revisa tu email", Toast.LENGTH_SHORT).show();
                                     dialog.dismiss();
                                 } else {
                                     Toast.makeText(LoginActivity.this, "No se pudo enviar el email de recuperacion", Toast.LENGTH_SHORT).show();
                                     dialog.dismiss();
                                 }
                             }
                         });
                    }
                });
                dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                if(dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                dialog.show();
            }
        });

    }

    // Método para manejar el login con google
    private void googleSignIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_ING);

    }

    // Sobrescritura del método para manejar resulados
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_ING) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (Exception ex) {
                 Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Manejo de la autenticación con firebase para guardar datos en base de datos firebase
    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", user.getUid());
                            map.put("name", user.getDisplayName());
                            map.put("photo", user.getPhotoUrl().toString());
                            database.getReference().child("users").child(user.getUid()).setValue(map);
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }else {
                            Toast.makeText(LoginActivity.this, "Algo salio mal", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}