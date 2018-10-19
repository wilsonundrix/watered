package com.wilsonundrix.watered;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class AccountFragment extends Fragment {

    private ProgressBar pbProf;
    private CircleImageView iv_Prof_image;
    private TextView tvProfEmail, tvProfUsername, tvProfFullNames, tvProfPhoneNo;
    private Button btnEditProf;
    private Uri mainImageURI;
    Context context;
    String user_id;
    private FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;

    private List<IdeaPosts> myPostsList = new ArrayList<>();
    private MinePostRecyclerAdapter myPostRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        context = container.getContext();

        pbProf = view.findViewById(R.id.pbProf);
        iv_Prof_image = view.findViewById(R.id.iv_profile_image);
        tvProfEmail = view.findViewById(R.id.tvProfEmail);
        tvProfUsername = view.findViewById(R.id.tvProfUsername);
        tvProfFullNames = view.findViewById(R.id.tvProfFullNames);
        tvProfPhoneNo = view.findViewById(R.id.tvProfPhoneNo);
        btnEditProf = view.findViewById(R.id.btnEditProf);

        btnEditProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AccountSettings.class);
                startActivity(intent);
            }
        });

        pbProf.setVisibility(View.VISIBLE);
        btnEditProf.setVisibility(View.INVISIBLE);

        iv_Prof_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShowProfilePic.class);
                intent.putExtra("any_user_id", user_id);
                intent.putExtra("any_user_name", "My Avatar");
                startActivity(intent);
            }
        });

        firebaseFirestore.collection("Users").document(user_id).get().
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

                                tvProfUsername.setText(username);
                                tvProfFullNames.setText(fullNames);
                                tvProfPhoneNo.setText(phoneNo);
                                tvProfEmail.setText(mAuth.getCurrentUser().getEmail());
                                mainImageURI = Uri.parse(image_uri);

                                RequestOptions placeHolderReq = new RequestOptions();
                                placeHolderReq.placeholder(R.drawable.ic_person);
                                Glide.with(context).setDefaultRequestOptions(placeHolderReq).load(image_uri).into(iv_Prof_image);

                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(getContext(), "Error : " + error, Toast.LENGTH_LONG).show();
                        }
                        pbProf.setVisibility(View.INVISIBLE);
                        btnEditProf.setEnabled(true);
                        btnEditProf.setVisibility(View.VISIBLE);
                    }
                });

        myPostsList = new ArrayList<>();
        RecyclerView my_post_list_view = view.findViewById(R.id.my_posts_view);
        myPostRecyclerAdapter = new MinePostRecyclerAdapter(myPostsList);

        my_post_list_view.setLayoutManager(new LinearLayoutManager(context));
//        post_list_view.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        my_post_list_view.setAdapter(myPostRecyclerAdapter);
        my_post_list_view.setHasFixedSize(true);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("idea_posts").whereEqualTo("user_id", user_id)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    myPostsList.add(doc.getDocument().toObject(IdeaPosts.class));
                                    myPostRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        }

                    }
                });


        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.acc_settings_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_acc_settings) {
            Intent intent = new Intent(getActivity(), AccountSettings.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_log_out) {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        } else if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
