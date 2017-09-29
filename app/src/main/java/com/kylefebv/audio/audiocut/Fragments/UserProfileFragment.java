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
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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


public class UserProfileFragment extends Fragment {

    private TextView mTextView;
    private static final String ARG_PARAM1 = "uid";
    private static final String ARG_PARAM2 = "name";
    private String name;
    private CircleImageView mCircleImageView;
    private TextView mFollowerText, mFollowingText;
    // private Button mFollowButton;
    private RecyclerView myRecyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManagerVertical;
    private FirebaseAdapter myRecyclerViewAdapter;
    StorageReference mStorageReference;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int followerCount, followingCount;

    public static ArrayList<String> followingNames, followerNames, followUUID, followingUUID, flwUUID, fingUUID;
    Context mContext;
    String mName;
    DatabaseReference ref;
    String uid;


    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
            //getFollow();
            name = getArguments().getString("name");
            setUpRecyclerAdapter();
            loadImage();
        }


        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getFollowerCount();
        getFollowingCount();

    }

    private void initLists() {
        followerNames = new ArrayList<>();
        followingNames = new ArrayList<>();
        followingUUID = new ArrayList<>();
        followUUID = new ArrayList<>();


    }

    private void setUpRecyclerAdapter() {

        myRecyclerViewAdapter = new FirebaseAdapter(getActivity(), firebaseDatabase, uid);
        myRecyclerViewAdapter.FirebaseAdapterAdd();
        myRecyclerView.setAdapter(myRecyclerViewAdapter);
        myRecyclerView.setLayoutManager(staggeredGridLayoutManagerVertical);

    }

    private void onFollowingClick() {
        mFollowingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new MaterialDialog.Builder(getActivity())
                        .title("Following")
                        .items(followingNames)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                Bundle b = new Bundle();
                                b.putString("uid", flwUUID.get(which));
                                ((MainActivity) mContext).switchToProfile(b);
                            }
                        })
                        .show();

            }
        });
    }



    private void getDatabaseRefs() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference("songs");
        databaseReference = firebaseDatabase.getReference("users");
    }



    private View setUpView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);
        initViews(v);


        mContext = getActivity();
        initLists();


        setLayout();

        return v;

    }

    private void setLayout(){
        staggeredGridLayoutManagerVertical = new StaggeredGridLayoutManager(2,LinearLayoutManager.VERTICAL);
    }

    private void initViews(View v){
        mTextView = (TextView) v.findViewById(nameText);
        mCircleImageView = (CircleImageView) v.findViewById(R.id.profileImg);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        //mFollowButton = (Button) v.findViewById(R.id.followbtn);
        mFollowerText = (TextView) v.findViewById(R.id.followers);
        mFollowingText = (TextView) v.findViewById(R.id.followings);
    }

    private void loadImage() {
        mStorageReference = FirebaseStorage.getInstance().getReference("userImages/").child(uid + ".png");
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(mStorageReference)
                .into(mCircleImageView);
        setTextView(name);
    }

    private void setTextView(String name) {
        mTextView.setText(name);
    }



    private void setFollowingText() {
        mFollowingText.setText(followingCount + "\n following");


    }

    private void getFollowerName(String s) {

        databaseReference.child(s).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mName = dataSnapshot.child("name").getValue().toString();
                followerNames.add(mName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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

    private void getFollowingName(String s) {
        databaseReference.child(s).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mName = dataSnapshot.child("name").getValue().toString();
                followingNames.add(mName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void setFollowerText() {
        mFollowerText.setText(followerCount + "\n followers");
    }

}
