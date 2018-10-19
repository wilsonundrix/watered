package com.wilsonundrix.watered;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ShowPost extends AppCompatActivity {

    private TextView tvPostTitle, tvPostDesc, tvPostUsername, tvPostTimestamp, ibPostFavText;
    private ImageView ivPostPic, ivPostSenderPic;
    FirebaseFirestore firebaseFirestore;
    private ImageButton ibPostFav;
    private Toolbar tbShowPost;
    private Uri mainImageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_post);

        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String cur_user = mAuth.getCurrentUser().getUid();
        final String post_id = getIntent().getExtras().getString("post_id");
        final String post_user = getIntent().getExtras().getString("any_user_name");

        tvPostTitle = findViewById(R.id.tvPostTitle);
        tvPostDesc = findViewById(R.id.tvPostDesc);
        tvPostUsername = findViewById(R.id.tvPostUsername);
        tvPostTimestamp = findViewById(R.id.tvPostTimestamp);
        ibPostFav = findViewById(R.id.ibPostFav);
        ibPostFavText = findViewById(R.id.ibPostFavText);

        tbShowPost = findViewById(R.id.tbShowPost);
        setSupportActionBar(tbShowPost);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivPostPic = findViewById(R.id.ivPostPic);
        ivPostSenderPic = findViewById(R.id.ivPostSenderPic);

        firebaseFirestore.collection("idea_posts").document(post_id).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
//                                Toast.makeText(Dashboard.this, "Data Exists", Toast.LENGTH_LONG).show();
                                String title = task.getResult().getString("title");
                                String description = task.getResult().getString("description");
                                String user_id = task.getResult().getString("user_id");
                                long timestamp = task.getResult().getTimestamp("timestamp").toDate().getTime();
                                String image_uri = task.getResult().getString("image_uri");

                                tvPostTitle.setText(title);
                                tvPostDesc.setText(description);
                                tvPostUsername.setText("Post by: " + post_user + "-----");
                                try {
                                    String dateString = DateFormat.format("MMMM dd, yyyy HH:mm ", new Date(timestamp)).toString();
                                    tvPostTimestamp.setText("Posted on " + dateString);
                                } catch (Exception e) {
                                    Toast.makeText(ShowPost.this, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }


                                mainImageURI = Uri.parse(image_uri);
                                getSupportActionBar().setTitle(post_user);

                                RequestOptions placeHolderReq = new RequestOptions();
                                placeHolderReq.placeholder(R.drawable.ic_person);
                                Glide.with(ShowPost.this).setDefaultRequestOptions(placeHolderReq).load(image_uri).into(ivPostPic);


                                firebaseFirestore.collection("Users").document(user_id).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    String userName = task.getResult().getString("username");
                                                    String userImage = task.getResult().getString("image_uri");

                                                    tvPostUsername.setText(userName);
                                                    RequestOptions requestImage = new RequestOptions();
                                                    requestImage.placeholder(R.drawable.ic_person);
                                                    Glide.with(ShowPost.this).applyDefaultRequestOptions(requestImage).load(userImage).into(ivPostSenderPic);
                                                }
                                            }
                                        });

                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(ShowPost.this, "Error : " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });


        firebaseFirestore.collection("idea_posts/" + post_id + "/likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                ibPostFavText.setText(queryDocumentSnapshots.size() + " likes");
                            } else {
                                ibPostFavText.setText("0 likes");
                            }
                        }

                    }
                });

        firebaseFirestore.collection("idea_posts/" + post_id + "/likes").document(cur_user)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {
                            if (documentSnapshot.exists()) {
                                ibPostFav.setImageResource(R.drawable.ic_favorite_red);
                            } else {
                                ibPostFav.setImageResource(R.drawable.ic_favorite_black);
                            }
                        }
                    }
                });


        ibPostFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseFirestore.collection("idea_posts/" + post_id + "/likes")
                        .document(cur_user).get().
                        addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (!task.getResult().exists()) {
                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());
                                    firebaseFirestore.collection("idea_posts/" + post_id + "/likes")
                                            .document(cur_user).set(likesMap);
                                } else {
                                    firebaseFirestore.collection("idea_posts/" + post_id + "/likes")
                                            .document(cur_user).delete();
                                }
                            }
                        });

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
