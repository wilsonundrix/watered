package com.wilsonundrix.watered;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewPost extends AppCompatActivity {

    private ImageView ivNewPostImage;
    private ProgressBar pbNewPost;
    private Uri mainImageURI;
    private EditText etNewIdeaTitle, etNewIdeaDesc;
    Button btnAddNewPost;
    FirebaseAuth mAuth;
    private String user_id;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        Toolbar tbNewPost = findViewById(R.id.tbNewPost);
        setSupportActionBar(tbNewPost);
        getSupportActionBar().setTitle("Add New Idea");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pbNewPost = findViewById(R.id.pbNewPost);
        pbNewPost.setVisibility(View.INVISIBLE);
        ivNewPostImage = findViewById(R.id.ivNewPostImage);
        etNewIdeaTitle = findViewById(R.id.etNewPostTitle);
        etNewIdeaDesc = findViewById(R.id.etNewPostDesc);
        btnAddNewPost = findViewById(R.id.btnAddNewIdea);

        ivNewPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(NewPost.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(NewPost.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        ImagePicker();
                    }
                } else {
                    ImagePicker();
                }
            }
        });

        btnAddNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pbNewPost.setVisibility(View.VISIBLE);
                final String title = etNewIdeaTitle.getText().toString().trim();
                final String desc = etNewIdeaDesc.getText().toString().trim();
                final String image = mainImageURI.toString();

                if (!TextUtils.isEmpty(title) && (mainImageURI != null)) {
                    if (!TextUtils.isEmpty(desc)) {
                        pbNewPost.setVisibility(View.VISIBLE);
                        //Action to save the Image and the details.
                        final String randomName = random();
                        final StorageReference photoStorageReference = storageReference.child("idea_posts").child(randomName + ".jpg");
                        photoStorageReference.putFile(mainImageURI).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    System.out.println("NDUAAAAAUUNYA" + task.getException().getMessage());
                                    throw task.getException();
                                }
                                return photoStorageReference.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    storeFirestore(downloadUri.toString(), title, desc, user_id);
                                    Intent intent = new Intent(NewPost.this, MyIdeas.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(NewPost.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        etNewIdeaDesc.setError("Description Required");
                    }
                } else {
                    etNewIdeaTitle.setError("Title Idea and image Required");
                }
                pbNewPost.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                ivNewPostImage.setImageURI(mainImageURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                System.out.println("NDUUOOOOOONYA" + error);
            }
        }
    }

    public void ImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(1, 1)
                .start(NewPost.this);
    }

    private void storeFirestore(String download_uri, String title, String description, String user_id) {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", title);
        postMap.put("description", description);
        postMap.put("user_id", user_id);
        postMap.put("image_uri", download_uri);
        postMap.put("timestamp", FieldValue.serverTimestamp());

        firebaseFirestore.collection("idea_posts").document().set(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NewPost.this, "The post was sent successfully.", Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(NewPost.this, MyIdeas.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(NewPost.this, "(FireStore Error) : " + error, Toast.LENGTH_LONG).show();
                    System.out.println("NDUUUUUUUUUUNYA" + error);
                }
//                pbAccSettings.setVisibility(View.INVISIBLE);
            }
        });
    }

    public static String random() {
        return UUID.randomUUID().toString();
    }
}
