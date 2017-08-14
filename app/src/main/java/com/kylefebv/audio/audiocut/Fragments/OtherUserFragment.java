package com.kylefebv.audio.audiocut.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
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
    HashMap<String, String> flwH;
    HashMap<String, String> fingH;
    public static ArrayList<String> followingNames, followerNames, followUUID, followingUUID, flwUUID, fingUUID;
    Context mContext;
    String mName;
    DatabaseReference ref;
    String uid;
    HashMap<String,String> h = new HashMap<>();



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
            getFollow();
            checkIfFollowing();
            setUpRecyclerAdapter();
            loadImage();
        }



        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        onFollowingClick();
        followerClick();



    }

    private void initLists(){
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

                                if(!uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    Bundle b = new Bundle();
                                    b.putString("uid", flwUUID.get(which));
                                    ((MainActivity) mContext).switchToProfile(b);
                                }else{
                                    Bundle b = new Bundle();
                                    b.putString("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    b.putString("name",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                                    ((MainActivity) mContext).switchToSingle(b, new UserProfileFragment());
                                }
                            }
                        })
                        .show();

            }
        });
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
                                if(flwUUID.get(which).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    Bundle b = new Bundle();
                                    Log.d("ddd",text.toString());
                                    b.putString("uid", flwUUID.get(which));
                                    ((MainActivity) mContext).switchToProfile(b);
                                }else{
                                    Bundle b = new Bundle();
                                    b.putString("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    b.putString("name",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                                    ((MainActivity) mContext).switchToSingle(b, new UserProfileFragment());
                                }
                            }
                        })
                        .show();
            }
        });
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


    private void removeFromFollowers(){
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = dataSnapshot.getValue(User.class);
                mUser.removeFollower(uid,FirebaseAuth.getInstance().getCurrentUser().getUid());
                flwH = mUser.getFollowers();
                dataSnapshot.getRef().child("followers").setValue(flwH);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addToFollowers(){
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = dataSnapshot.getValue(User.class);
                mUser.addToFollowers(uid,FirebaseAuth.getInstance().getCurrentUser().getUid());
                flwH = mUser.getFollowers();
                dataSnapshot.getRef().child("followers").setValue(flwH);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addToFollowing(){
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = dataSnapshot.getValue(User.class);
                mUser.addToFollowing(uid,FirebaseAuth.getInstance().getCurrentUser().getUid());
                flwH = mUser.getFollowers();
                dataSnapshot.getRef().child("following").setValue(flwH);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeFromFollowing(){
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = dataSnapshot.getValue(User.class);
                mUser.removeFollowing(uid,FirebaseAuth.getInstance().getCurrentUser().getUid());
                flwH = mUser.getFollowers();
                dataSnapshot.getRef().child("following").setValue(flwH);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setClickFollow(){


        mFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(areFollowing){
                    setNotFollowing();
                    removeFromFollowers();
                    removeFromFollowing();
                }else{

                    setAreFollowing();
                    addToFollowers();
                    addToFollowing();
                }



            }
        });
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

    private void getFollowers() {

        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User mUser = dataSnapshot.getValue(User.class);
                flwH = mUser.getFollowers();

                flwUUID = mUser.setHashToList(flwUUID,flwH);
                matchFollowList(flwUUID);
                followerCount = mUser.followersCount();

                setFollowerText();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


    }

    private void matchFollowList(ArrayList<String> s ){
        for(String d : s){
            getFollowerName(d);
        }
    }



    private void matchToFollowingList(ArrayList<String> e){
        for(String d : e){
            getFollowingName(d);
        }

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

    private void getFollowing() {
        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = dataSnapshot.getValue(User.class);
                fingH =  mUser.getFollowing();
                fingUUID = mUser.setHashToList(fingUUID,fingH);
                matchToFollowingList(fingUUID);

                followingCount = mUser.followingCount();

                setFollowingText();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setFollowingText() {
        mFollowingText.setText(followingCount + "\n following");


    }

    private void getFollowerName(String s){

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

    private void getFollowingName(String s){
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

    private void setFollowerText(){
        mFollowerText.setText(followerCount + "\n followers");
    }

    private void checkIfFollowing(){
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = dataSnapshot.getValue(User.class);
                flwH = mUser.getFollowing();
                flwUUID = mUser.setHashToList(flwUUID,flwH);
                checkUUID(flwUUID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

}
