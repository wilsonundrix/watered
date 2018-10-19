package com.wilsonundrix.watered;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class HomeFragment extends Fragment {

    private List<IdeaPosts> postsList = new ArrayList<>();
    private PostRecyclerAdapter postRecyclerAdapter;

    FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        FloatingActionButton fabMyIdeas = view.findViewById(R.id.fabMyIdeas);
        postsList = new ArrayList<>();
        RecyclerView post_list_view = view.findViewById(R.id.post_list_view);
        postRecyclerAdapter = new PostRecyclerAdapter(postsList);

        post_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
//        post_list_view.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        post_list_view.setAdapter(postRecyclerAdapter);
        post_list_view.setHasFixedSize(true);

        if (mAuth.getCurrentUser() == null) {
            SendToLogin();
        } else {

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("idea_posts").orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots != null) {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        String post_id = doc.getDocument().getId();
                                        IdeaPosts ideaPosts = doc.getDocument().toObject(IdeaPosts.class).withId(post_id);
                                        postsList.add(ideaPosts);
                                        postRecyclerAdapter.notifyDataSetChanged();
                                    }
                                }
                            }

                        }
                    });
        }


        fabMyIdeas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getContext(), "New Idea Btn", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), NewPost.class);
                startActivity(intent);
            }
        });
        return view;
    }

    private void SendToLogin() {
        Intent settingsIntent = new Intent(getContext(), Login.class);
        startActivity(settingsIntent);
        getActivity().finish();
    }
}
