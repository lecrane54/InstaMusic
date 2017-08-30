package com.kylefebv.audio.audiocut.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kylefebv.audio.audiocut.Adapters.RecyclerAdapter;
import com.kylefebv.audio.audiocut.R;


public class UserFeedFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private RecyclerView mRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerAdapter adapter;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference ref;
    private String uid;
    private int progress;


    public UserFeedFragment() {
        // Required empty public constructor
    }


    public static UserFeedFragment newInstance(String param1, String param2) {
        UserFeedFragment fragment = new UserFeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_feed, container, false);
        initView(v);
        initFirebase();

        setRecyclerViewAdapter();


        return v;
    }

    private void setRecyclerViewAdapter() {
        adapter = new RecyclerAdapter(getActivity(), firebaseDatabase, uid);
        mRecyclerView.setAdapter(adapter);
    }

    private void initView(View v) {
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void initFirebase() {
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference("songs");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        adapter.mMediaPlayer.release();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("dd", "paus");
        adapter.mMediaPlayer.pause();
        progress = adapter.mMediaPlayer.getCurrentPosition();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.mMediaPlayer.reset();
        adapter.mMediaPlayer.release();
    }
}


