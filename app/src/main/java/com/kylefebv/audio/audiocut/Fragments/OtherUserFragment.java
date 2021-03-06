package com.kylefebv.audio.audiocut.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kylefebv.audio.audiocut.Activities.MainActivity;
import com.kylefebv.audio.audiocut.Adapters.FirebaseAdapter;
import com.kylefebv.audio.audiocut.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.kylefebv.audio.audiocut.R.id.nameText;


public class OtherUserFragment extends Fragment {

    private TextView mTextView;
    private static final String ARG_PARAM1 = "uid";

    private String name;
    private CircleImageView mCircleImageView;
    private TextView mFollowerText, mFollowingText;
    private Button mFollowButton;
    boolean areFollowing = false;
    private RecyclerView myRecyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManagerVertical;
    private FirebaseAdapter myRecyclerViewAdapter;
    StorageReference mStorageReference;
    FirebaseStorage storage;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int followerCount, followingCount;

    public static ArrayList<String> followingNames, followerNames, followUUID, followingUUID, flwUUID, fingUUID;
    Context mContext;
    String mName;
    DatabaseReference ref;
    String uid;




    public OtherUserFragment() {
        // Required empty public constructor
    }

    public static OtherUserFragment newInstance(String param1) {
        OtherUserFragment fragment = new OtherUserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = setUpView(inflater, container, savedInstanceState);
        getDatabaseRefs();
        if (getArguments() != null) {
            uid = getArguments().getString("uid");

            if(uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                Bundle b = new Bundle();
                b.putString("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                b.putString("name",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                ((MainActivity) mContext).switchToSingle(b, new UserProfileFragment());
            }
            getName();


            setUpRecyclerAdapter();
            loadImage();
        }



        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isFollowing();
        getFollowerCount();
        getFollowingCount();
    }

    private void initLists(){
        followerNames = new ArrayList<>();
        followingNames = new ArrayList<>();
        followingUUID = new ArrayList<>();
        followUUID = new ArrayList<>();
        flwUUID = new ArrayList<>();
        fingUUID = new ArrayList<>();

    }

    private void setUpRecyclerAdapter() {

        myRecyclerViewAdapter = new FirebaseAdapter(getActivity(), firebaseDatabase, uid);
        myRecyclerViewAdapter.FirebaseAdapterAdd();
        myRecyclerView.setAdapter(myRecyclerViewAdapter);
        myRecyclerView.setLayoutManager(staggeredGridLayoutManagerVertical);

    }


    private void getDatabaseRefs() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference("songs");
        databaseReference = firebaseDatabase.getReference("users");
    }



    private View setUpView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_other_user, container, false);
        mTextView = (TextView) v.findViewById(nameText);
        mCircleImageView = (CircleImageView) v.findViewById(R.id.profileImg);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mFollowButton = (Button) v.findViewById(R.id.followbtn);
        mFollowerText = (TextView) v.findViewById(R.id.followers);
        mFollowingText = (TextView) v.findViewById(R.id.followings);
        setNotFollowing();


        setClickFollow();


        mContext = getActivity();
        initLists();


        staggeredGridLayoutManagerVertical =
                new StaggeredGridLayoutManager(
                        2, //The number of Columns in the grid
                        LinearLayoutManager.VERTICAL);

        return v;

    }

    private void loadImage() {
        mStorageReference = FirebaseStorage.getInstance().getReference("userImages/").child(uid + ".png");
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(mStorageReference)
                .into(mCircleImageView);

    }


    private void setClickFollow(){

        mFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(areFollowing){
                    setNotFollowing();
                    unfollowUser();
                    getFollowingCount();
                    getFollowerCount();
                }else{
                    followUser();
                    setAreFollowing();
                    getFollowingCount();
                    getFollowerCount();
                }
            }
        });
    }

    private void isFollowing(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("following")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild("uid")
                .equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    setAreFollowing();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void followUser(){
        firebaseDatabase.getReference("following")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(uid)
                .child("uid")
                .setValue(uid);

        firebaseDatabase.getReference("followers")
                .child(uid)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("uid")
                .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());


    }

    private void unfollowUser(){
        firebaseDatabase.getReference("following")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(uid)
                .child("uid")
                .removeValue();

        firebaseDatabase.getReference("followers")
                .child(uid)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("uid")
                .removeValue();

    }

    private void setAreFollowing(){
        mFollowButton.setText("unfollow");
        mFollowButton.setBackgroundColor(getResources().getColor(R.color.green_complete));
        mFollowButton.setTextColor(getResources().getColor(R.color.WhiteSmoke));
        areFollowing = true;


    }


    private void setNotFollowing(){
        mFollowButton.setText("Follow");
        mFollowButton.setBackgroundColor(getResources().getColor(R.color.blue_normal));
        mFollowButton.setTextColor(getResources().getColor(R.color.WhiteSmoke));
        areFollowing = false;


    }


    private void getName(){
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                setTextView(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setTextView(String name) {
        mTextView.setText(name);
    }





    private void checkUUID(ArrayList<String> e){
        if(e.contains(uid)){
            areFollowing = true;
        }else {
            areFollowing = false;
        }

        setButton(areFollowing);
    }

    private void setButton(boolean b){
        if(b){
            setAreFollowing();
        }else{
            setNotFollowing();
        }
    }

    private void getFollowerCount(){
        followerCount = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("followers").child(uid);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    followerCount++;
                }
                setFollowerText();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getFollowingCount(){
        followingCount = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("following")
                .child(uid);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    followingCount++;
                }
                setFollowingText();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        setFollowingText();
    }


    private void setFollowingText() {
        mFollowingText.setText(followingCount + "\n following");
    }

    private void getFollowerName(String s){

        databaseReference.child(s).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mName = dataSnapshot.child("name").getValue().toString();
                //followerNames.add(mName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void getFollowingName(String s){
        databaseReference.child(s).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mName = dataSnapshot.child("name").getValue().toString();
                //followingNames.add(mName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    private void setFollowerText(){
        mFollowerText.setText(followerCount + "\n followers");
    }



}
