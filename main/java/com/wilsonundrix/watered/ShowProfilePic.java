package com.wilsonundrix.watered;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShowProfilePic extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile_pic);

        firebaseFirestore = FirebaseFirestore.getInstance();

        Toolbar tbProfPic = findViewById(R.id.tbProfPic);
        setSupportActionBar(tbProfPic);
        final ImageView ivProfPic = findViewById(R.id.ivProfPic);

        String any_user_id = getIntent().getExtras().getString("any_user_id");
        String any_user_name = getIntent().getExtras().getString("any_user_name");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(any_user_name);

        firebaseFirestore.collection("Users").document(any_user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String userImage = task.getResult().getString("image_uri");
                            Glide.with(ShowProfilePic.this).load(userImage).into(ivProfPic);
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
