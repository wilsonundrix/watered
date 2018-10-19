package com.wilsonundrix.watered;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class MinePostRecyclerAdapter extends RecyclerView.Adapter<MinePostRecyclerAdapter.MyViewHolder> {

    private List<IdeaPosts> postsList;
    public Context context;
    private FirebaseFirestore firebaseFirestore;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView descView, titleView, tvUsername, timestampView, ibFavText;
        RelativeLayout rlFirst;
        ImageView ivImage, ivSenderPic;
        ImageButton ibFav;

        MyViewHolder(View itemView) {
            super(itemView);
            rlFirst = itemView.findViewById(R.id.rlFirst);
            descView = itemView.findViewById(R.id.tvIdeaDesc);
            titleView = itemView.findViewById(R.id.tvIdeaTitle);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivSenderPic = itemView.findViewById(R.id.ivSenderPic);
            ivImage = itemView.findViewById(R.id.ivIdeaPic);
            timestampView = itemView.findViewById(R.id.tvIdeaTimestamp);
            ibFav = itemView.findViewById(R.id.ibFav);
            ibFavText = itemView.findViewById(R.id.ibFavText);
        }
    }

    MinePostRecyclerAdapter(List<IdeaPosts> postsList) {
        this.postsList = postsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_view, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String cur_user = mAuth.getCurrentUser().getUid();
        final String post_id = postsList.get(position).IdeaPostId;

        final IdeaPosts idea = postsList.get(position);
        holder.descView.setText(idea.getDescription());
        holder.titleView.setText(idea.getTitle());

        //Setting the image
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.ndunyaheader);
        Glide.with(context).applyDefaultRequestOptions(requestOptions).load(idea.getImage_uri()).into(holder.ivImage);

        //user info
        final String user_id = idea.getUser_id();
        //User Data will be retrieved here...
        firebaseFirestore.collection("Users").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String userName = task.getResult().getString("username");
                            String userImage = task.getResult().getString("image_uri");

                            holder.tvUsername.setText(userName);
                            RequestOptions requestImage = new RequestOptions();
                            requestImage.placeholder(R.drawable.ic_person);
                            Glide.with(context).applyDefaultRequestOptions(requestImage).load(userImage).into(holder.ivSenderPic);
                        }
                    }
                });

        try {
            long millisecond = idea.getTimestamp().getTime();
            String dateString = DateFormat.format("MMMM dd, yyyy HHH:mm ", new Date(millisecond)).toString();
            holder.timestampView.setText(dateString);
        } catch (Exception e) {
            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        holder.ivSenderPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShowProfilePic.class);
                intent.putExtra("any_user_id", user_id);
                intent.putExtra("any_user_name", holder.tvUsername.getText().toString());
                context.startActivity(intent);
            }
        });

        firebaseFirestore.collection("idea_posts/" + post_id + "/likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                holder.ibFavText.setText(queryDocumentSnapshots.size() + " likes");
                            } else {
                                holder.ibFavText.setText("0 likes");
                            }
                        }

                    }
                });

        firebaseFirestore.collection("idea_posts/" + post_id + "/likes")
                .document(cur_user)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

//                        Toast.makeText(context, "Hapa ndo ngori", Toast.LENGTH_LONG).show();

                        if (documentSnapshot != null) {
                            if (documentSnapshot.exists()) {
                                holder.ibFav.setImageResource(R.drawable.ic_favorite_red);
                            } else {
                                holder.ibFav.setImageResource(R.drawable.ic_favorite_black);
                            }
                        }
                    }
                });


        holder.ibFav.setOnClickListener(new View.OnClickListener() {
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
    public int getItemCount() {
        return postsList.size();
    }
}
