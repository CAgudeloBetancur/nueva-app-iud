package com.iud.nuevaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText registerEmail, registerPassword, username;
    private Button registerBtn;
    private TextView ingresaAqui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Autenticacion
        auth = FirebaseAuth.getInstance();
        username = findViewById(R.id.registerUsername);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerBtn = findViewById(R.id.btnRegister);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = registerEmail.getText().toString().trim();
                String password = registerPassword.getText().toString().trim();
                if(email.isEmpty()) {
                    registerEmail.setError("El email no puede estar vacío");
                } else if (password.isEmpty()) {
                    registerPassword.setError("La contraseña no puede estar vacía");
                } else {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           if(task.isSuccessful()) {
                               Toast.makeText(RegisterActivity.this, "Registro Exitoso", Toast.LENGTH_SHORT).show();
                               startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                           } else {
                               Toast.makeText(RegisterActivity.this, "Registro fallido", Toast.LENGTH_SHORT).show();
                           }
                       }
                    });
                }
            }
        });
        // Redireccionar hacia login
        ingresaAqui = findViewById(R.id.ingresaAqui);
        ingresaAqui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this , LoginActivity.class));
            }
        });
    }
}