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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettings extends AppCompatActivity {

    private ProgressBar pbAccSettings;
    private CircleImageView iv_acc_settings_image;
    private EditText etAccUsername, etAccFullNames, etAccPhoneNo;
    private Uri mainImageURI;
    private Bitmap compressedImageFile;
    private String user_id;

    private boolean isChanged = false;

    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        Toolbar tbAccSettings = findViewById(R.id.tbAccSettings);
        pbAccSettings = findViewById(R.id.pbAccSettings);
        TextView tvAccEmail = findViewById(R.id.tvAccEmail);
        etAccUsername = findViewById(R.id.etAccUsername);
        etAccFullNames = findViewById(R.id.etAccFullNames);
        etAccPhoneNo = findViewById(R.id.etAccPhoneNo);
        Button btnSaveAcc = findViewById(R.id.btnSaveAcc);

        setSupportActionBar(tbAccSettings);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pbAccSettings.setVisibility(View.VISIBLE);
        tvAccEmail.setText(mAuth.getCurrentUser().getEmail());

        firebaseFirestore.collection("Users").document(user_id).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
//                                Toast.makeText(AccountSettings.this, "Data Exists", Toast.LENGTH_LONG).show();
                                String username = task.getResult().getString("username");
                                String fullNames = task.getResult().getString("fullNames");
                                String phoneNo = task.getResult().getString("phoneNo");
                                String image_uri = task.getResult().getString("image_uri");

                                etAccUsername.setText(username);
                                etAccFullNames.setText(fullNames);
                                etAccPhoneNo.setText(phoneNo);

                                mainImageURI = Uri.parse(image_uri);
                                RequestOptions placeHolder = new RequestOptions();
                                placeHolder.placeholder(R.drawable.ic_person);
                                Glide.with(AccountSettings.this).setDefaultRequestOptions(placeHolder).load(image_uri).into(iv_acc_settings_image);

                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(AccountSettings.this, "Error : " + error, Toast.LENGTH_LONG).show();
                        }
                        pbAccSettings.setVisibility(View.INVISIBLE);
                    }
                });

        btnSaveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pbAccSettings.setVisibility(View.VISIBLE);
                pbAccSettings.setIndeterminate(true);
                final String username = etAccUsername.getText().toString().trim();
                final String fullNames = etAccFullNames.getText().toString().trim();
                final String phoneNo = etAccPhoneNo.getText().toString().trim();
                if (!TextUtils.isEmpty(username) && (mainImageURI != null)) {
                    if (!TextUtils.isEmpty(fullNames)) {
                        if (!TextUtils.isEmpty(phoneNo)) {
                            if (isChanged) {
                                pbAccSettings.setVisibility(View.VISIBLE);
                                final StorageReference photoStorageReference = storageReference.child("Users").child(user_id + ".jpg");

                                photoStorageReference.putFile(mainImageURI).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }
                                        return photoStorageReference.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri downloadUri = task.getResult();
                                            storeFirestore(downloadUri, username, fullNames, phoneNo);
                                        } else {
                                            Toast.makeText(AccountSettings.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            } else {
                                storeFirestore(mainImageURI, username, fullNames, phoneNo);
                            }
                        } else {
                            etAccPhoneNo.setError("Phone Number Required");
                        }
                    } else {
                        etAccFullNames.setError("Names Are Required");
                    }
                } else {
                    etAccUsername.setError("Username Required and Profile Pic Required");
                }
                pbAccSettings.setVisibility(View.INVISIBLE);
            }
        });

        iv_acc_settings_image = findViewById(R.id.iv_acc_settings_image);
        iv_acc_settings_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(AccountSettings.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(AccountSettings.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        ImagePicker();
                    }
                } else {
                    ImagePicker();
                }
            }
        });
    }

    private void storeFirestore(Uri download_uri, String username, String fullNames, String phoneNo) {

        Map<String, String> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("fullNames", fullNames);
        userMap.put("phoneNo", phoneNo);
        userMap.put("image_uri", download_uri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AccountSettings.this, "The user Settings are updated.", Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(AccountSettings.this, MyIdeas.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AccountSettings.this, "(FireStore Error) : " + error, Toast.LENGTH_LONG).show();
                }
                pbAccSettings.setVisibility(View.INVISIBLE);
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
                isChanged = true;
                iv_acc_settings_image.setImageURI(mainImageURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void ImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(1, 1)
                .start(AccountSettings.this);
    }
}
