package com.wilsonundrix.watered;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Intent intentLogin = new Intent(MainActivity.this, Login.class);
            startActivity(intentLogin);
            finish();

        } else {
            firebaseFirestore.collection("Users").document(user.getUid()).get().
                    addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().exists()) {
                                    Intent intentDash = new Intent(MainActivity.this, MyIdeas.class);
                                    startActivity(intentDash);
                                    finish();
                                } else {
                                    Intent intentAcc = new Intent(MainActivity.this, Login.class);
                                    startActivity(intentAcc);
                                    finish();
                                }
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(MainActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }

    }
}
