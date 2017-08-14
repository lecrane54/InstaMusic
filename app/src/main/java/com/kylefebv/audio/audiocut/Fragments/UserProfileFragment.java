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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kylefebv.audio.audiocut.Activities.MainActivity;
import com.kylefebv.audio.audiocut.Adapters.FirebaseAdapter;
import com.kylefebv.audio.audiocut.R;
import com.kylefebv.audio.audiocut.Models.User;

import java.util.ArrayList;
import java.util.HashMap;

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
    FirebaseStorage storage;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int followerCount, followingCount;
    HashMap<String, String> flwH;
    HashMap<String, String> fingH;
    public static ArrayList<String> followingNames, followerNames, followUUID, followingUUID, flwUUID, fingUUID;
    Context mContext;
    String mName;
    DatabaseReference ref;
    String uid;
    HashMap<String, String> h = new HashMap<>();


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


       // onFollowingClick();
       // followerClick();


    }

    private void initLists() {
        followerNames = new ArrayList<>();
        followingNames = new ArrayList<>();
        followingUUID = new ArrayList<>();
        followUUID = new ArrayList<>();
        flwUUID = new ArrayList<>();
        fingUUID = new ArrayList<>();
        fingH = new HashMap<>();
        flwH = new HashMap<>();
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

    private void getUidAndNames() {

    }

    private void getFollow() {
        getFollowing();
        getFollowers();
    }

    private void getDatabaseRefs() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference("songs");
        databaseReference = firebaseDatabase.getReference("users");
    }

    private void followerClick() {
        mFollowerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity())
                        .title("Followers")
                        .items(followerNames)
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

    private View setUpView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);
        mTextView = (TextView) v.findViewById(nameText);
        mCircleImageView = (CircleImageView) v.findViewById(R.id.profileImg);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        //mFollowButton = (Button) v.findViewById(R.id.followbtn);
        mFollowerText = (TextView) v.findViewById(R.id.followers);
        mFollowingText = (TextView) v.findViewById(R.id.followings);


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
        setTextView(name);
    }

    private void setTextView(String name) {
        mTextView.setText(name);
    }

    private void getFollowers() {

        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User mUser = dataSnapshot.getValue(User.class);
                flwH = mUser.getFollowers();

                flwUUID = mUser.setHashToList(flwUUID, flwH);
                matchFollowList(flwUUID);
                followerCount = mUser.followersCount();

                setFollowerText(followerCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    private void matchFollowList(ArrayList<String> s) {
        for (String d : s) {
            getFollowerName(d);
        }
    }


    private void matchToFollowingList(ArrayList<String> e) {
        for (String d : e) {
            if(!d.equals(uid)) {
                getFollowingName(d);
            }else{
                followingCount = followerCount -1;
            }
        }

    }

    private void getFollowing() {
        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = dataSnapshot.getValue(User.class);
                fingH = mUser.getFollowing();
                fingUUID = mUser.setHashToList(fingUUID, fingH);
                matchToFollowingList(fingUUID);

                followingCount = mUser.followingCount() -1;

                setFollowingText(followingCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setFollowingText(int count) {
        mFollowingText.setText(count + "\n following");


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

    private void setFollowerText(int count) {
        mFollowerText.setText(count + "\n followers");
    }

}
