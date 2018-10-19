package com.wilsonundrix.watered;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private EditText etLoginEmail, etLoginPass;
    private Button btnLogin, btnLoginReg;
    private ProgressBar pbLogin;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPass = findViewById(R.id.etLoginPass);

        btnLogin = findViewById(R.id.btnLogin);
        btnLoginReg = findViewById(R.id.btnLoginReg);

        pbLogin = findViewById(R.id.pbLogin);
        pbLogin.setVisibility(View.INVISIBLE);

        btnLogin.setOnClickListener(this);
        btnLoginReg.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(Login.this, MyIdeas.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onClick(View view) {
        if (view == btnLogin) {

            pbLogin.setVisibility(View.VISIBLE);

            String email = etLoginEmail.getText().toString().trim();
            String pass = etLoginPass.getText().toString().trim();

            if (!email.isEmpty()) {
                if (!pass.isEmpty()) {
//                    Toast.makeText(Login.this, "Ready to Login..", Toast.LENGTH_SHORT).show();

                    //login process
                    mAuth.signInWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Intent intent = new Intent(Login.this, MyIdeas.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Login.this, "Authentication failed."
                                                + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                } else {
                    etLoginPass.setError("Password is required");
                }
            } else {
                etLoginEmail.setError("Email Required");
            }
            pbLogin.setVisibility(View.INVISIBLE);

        } else if (view == btnLoginReg) {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
            finish();
        }
    }
}
