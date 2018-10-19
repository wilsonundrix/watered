package com.wilsonundrix.watered;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class OtherUser extends AppCompatActivity {

    private TextView tvOtherUserId;
    private String other_user_id;
    private ProgressBar pbOther;
    private CircleImageView iv_Other_image;
    private TextView tvOtherUsername, tvOtherFullNames, tvOtherPhoneNo;
    private Button btnTextOther;
    private Uri mainImageURI;
    FirebaseFirestore firebaseFirestore;
    private static final int PERMISSION_REQUEST_CODE = 1;
    AlertDialog.Builder builder;


    private List<IdeaPosts> otherPostsList = new ArrayList<>();
    private MinePostRecyclerAdapter otherPostRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user);

        other_user_id = getIntent().getExtras().getString("other_user_id");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        builder = new AlertDialog.Builder(this);
        firebaseFirestore = FirebaseFirestore.getInstance();

        Toolbar tbOther = findViewById(R.id.tbOther);
        setSupportActionBar(tbOther);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pbOther = findViewById(R.id.pbOther);
        iv_Other_image = findViewById(R.id.iv_OtherProfile_image);
        tvOtherUsername = findViewById(R.id.tvOtherUsername);
        tvOtherFullNames = findViewById(R.id.tvOtherFullNames);
        tvOtherPhoneNo = findViewById(R.id.tvOtherPhoneNo);
        btnTextOther = findViewById(R.id.btnTextOther);

        iv_Other_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OtherUser.this, ShowProfilePic.class);
                intent.putExtra("any_user_id", other_user_id);
                intent.putExtra("any_user_name", tvOtherUsername.getText().toString());
                startActivity(intent);
            }
        });

        btnTextOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                    if (checkSelfPermission(Manifest.permission.SEND_SMS) == PERMISSION_DENIED) {

                        Log.d("permission", "permission denied to SEND_SMS - requesting it");
                        String[] permissions = {Manifest.permission.SEND_SMS};

                        requestPermissions(permissions, PERMISSION_REQUEST_CODE);

                    } else {
                        final Dialog dialog = new Dialog(OtherUser.this);
                        dialog.setContentView(R.layout.send_sms_layout);
                        dialog.setTitle("Send SMS");

                        TextView tvMsg = dialog.findViewById(R.id.tvMessage);
                        final EditText etMsg = dialog.findViewById(R.id.etMessage);
                        Button btnMsg = dialog.findViewById(R.id.btnSendMessage);
                        Button btnCancel = dialog.findViewById(R.id.btnCancel);
                        tvMsg.setText("Send SMS to " + tvOtherUsername.getText().toString());
                        btnMsg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String theMsg = etMsg.getText().toString().trim();
                                sendSMS(theMsg);
                                dialog.dismiss();
                            }
                        });
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                }else{
                    final Dialog dialog = new Dialog(OtherUser.this);
                    dialog.setContentView(R.layout.send_sms_layout);
                    dialog.setTitle("Send SMS");

                    TextView tvMsg = dialog.findViewById(R.id.tvMessage);
                    final EditText etMsg = dialog.findViewById(R.id.etMessage);
                    Button btnMsg = dialog.findViewById(R.id.btnSendMessage);
                    Button btnCancel = dialog.findViewById(R.id.btnCancel);
                    tvMsg.setText("Send SMS to " + tvOtherUsername.getText().toString());
                    btnMsg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String theMsg = etMsg.getText().toString().trim();
                            sendSMS(theMsg);
                            dialog.dismiss();
                        }
                    });
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        });

        firebaseFirestore.collection("Users").document(other_user_id).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
//                                Toast.makeText(Dashboard.this, "Data Exists", Toast.LENGTH_LONG).show();
                                String username = task.getResult().getString("username");
                                String fullNames = task.getResult().getString("fullNames");
                                String phoneNo = task.getResult().getString("phoneNo");
                                String image_uri = task.getResult().getString("image_uri");

                                tvOtherUsername.setText(username);
                                tvOtherFullNames.setText(fullNames);
                                tvOtherPhoneNo.setText(phoneNo);
                                mainImageURI = Uri.parse(image_uri);
                                getSupportActionBar().setTitle(username);

                                RequestOptions placeHolderReq = new RequestOptions();
                                placeHolderReq.placeholder(R.drawable.ic_person);
                                Glide.with(OtherUser.this).setDefaultRequestOptions(placeHolderReq).load(image_uri).into(iv_Other_image);

                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(OtherUser.this, "Error : " + error, Toast.LENGTH_LONG).show();
                        }
                        pbOther.setVisibility(INVISIBLE);
                        btnTextOther.setEnabled(true);
                        btnTextOther.setVisibility(VISIBLE);
                    }
                });


        otherPostsList = new ArrayList<>();
        RecyclerView other_post_list_view = findViewById(R.id.other_posts_view);
        otherPostRecyclerAdapter = new MinePostRecyclerAdapter(otherPostsList);

        other_post_list_view.setLayoutManager(new LinearLayoutManager(this));
//        post_list_view.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        other_post_list_view.setAdapter(otherPostRecyclerAdapter);
        other_post_list_view.setHasFixedSize(true);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("idea_posts").whereEqualTo("user_id", other_user_id)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    otherPostsList.add(doc.getDocument().toObject(IdeaPosts.class));
                                    otherPostRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        }

                    }
                });


    }

    void sendSMS(String msg) {
        /** Getting an instance of SmsManager to sent sms message from the application*/
        SmsManager smsManager = SmsManager.getDefault();
        /** Sending the Sms message to the intended party */
        smsManager.sendTextMessage(tvOtherPhoneNo.getText().toString(), null, msg, null, null);
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
