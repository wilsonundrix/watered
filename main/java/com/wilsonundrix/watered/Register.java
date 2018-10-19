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

public class Register extends AppCompatActivity implements View.OnClickListener {

    private EditText etRegEmail, etRegPass, etRegConfPass;
    private Button btnReg, btnRegLogin;
    private ProgressBar pbReg;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPass = findViewById(R.id.etRegPass);
        etRegConfPass = findViewById(R.id.etRegConfPass);

        btnReg = findViewById(R.id.btnReg);
        btnRegLogin = findViewById(R.id.btnRegLogin);

        pbReg = findViewById(R.id.pbReg);
        pbReg.setVisibility(View.INVISIBLE);

        btnReg.setOnClickListener(this);
        btnRegLogin.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View view) {
        if (view == btnReg) {

            pbReg.setVisibility(View.VISIBLE);

            String email = etRegEmail.getText().toString().trim();
            String pass = etRegPass.getText().toString().trim();
            String conf_pass = etRegConfPass.getText().toString().trim();

            if (!email.isEmpty()) {
                if (!pass.isEmpty()) {
                    if (!conf_pass.isEmpty()) {
                        if (pass.equals(conf_pass)) {
//                            Toast.makeText(Register.this, "Ready to Register..", Toast.LENGTH_SHORT).show();
                            //Reg process
                            mAuth.createUserWithEmailAndPassword(email, pass)
                                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
//                                                final FirebaseUser user = mAuth.getCurrentUser();
//                                                assert user != null;
//                                                user.sendEmailVerification()
//                                                        .addOnCompleteListener(Register.this, new OnCompleteListener<Void>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                if (task.isSuccessful()) {
//                                                                    Toast.makeText(Register.this,
//                                                                            "Verification Email sent to.." + user.getEmail(), Toast.LENGTH_SHORT).show();
//                                                                } else {
//                                                                    Toast.makeText(Register.this,
//                                                                            "Failed to send Verification Email...", Toast.LENGTH_SHORT).show();
//                                                                }
//                                                            }
//                                                        });
                                                Intent intent = new Intent(Register.this, AccountSettings.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Toast.makeText(Register.this, "Authentication failed."
                                                                + task.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                        } else {
                            etRegConfPass.setError("Passwords don't match");
                        }
                    } else {
                        etRegConfPass.setError("Confirm your Password");
                    }
                } else {
                    etRegPass.setError("Password is required");
                }
            } else {
                etRegEmail.setError("Email Required");
            }
            pbReg.setVisibility(View.INVISIBLE);

        } else if (view == btnRegLogin) {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        }
    }
}