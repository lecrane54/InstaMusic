package com.kylefebv.audio.audiocut.Fragments;


import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kylefebv.audio.audiocut.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SingleSongFragment extends Fragment {
    private static final String ARG_PARAM1 = "uuid";
    private static final String ARG_PARAM2 = "sRef";
    private static final String ARG_PARAM3 = "title";
    private TextView mTextView , nameTextView;
    private CircleImageView mCircleImageView;
    private RoundedImageView mRoundedImageView;
    private String name,title;
    private String songMp3;
    private MediaPlayer mMediaPlayer;
    String uid;
    private int play = 0;
    RotateAnimation rotateAnimation;
    StorageReference mStorageReference2;
    StorageReference mStorageReference;
    FirebaseStorage storage;
    FirebaseDatabase firebaseDatabase;


    DatabaseReference ref;

    public SingleSongFragment() {
        // Required empty public constructor
    }



    public static SingleSongFragment newInstance(String param1, String param2, String param3) {
        SingleSongFragment fragment = new SingleSongFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2,param2);
        args.putString(ARG_PARAM3,param3);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_single_song, container, false);
        mTextView = (TextView)v.findViewById(R.id.songName);
        nameTextView = (TextView)v.findViewById(R.id.profileName);
        mCircleImageView = (CircleImageView)v.findViewById(R.id.profileImg1);
        mRoundedImageView = (RoundedImageView)v.findViewById(R.id.imageView1);
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
       // ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        rotateAnimation = new RotateAnimation(0, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(1200);
        rotateAnimation.setRepeatCount(Animation.INFINITE);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(false);
        uid = u.getUid();
        name = u.getDisplayName();
        if(getArguments() != null){
            songMp3 = getArguments().getString("uuid");
            title = getArguments().getString("title");

            nameTextView.setText(name);
            mTextView.setText(title);
            loadSongImage();
        }
        loadImage();

        mRoundedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(play == 0) {
                    fetchAudioUrlFromFirebase();
                    mRoundedImageView.startAnimation(rotateAnimation);

                }else{
                    stopMusic();
                    mRoundedImageView.clearAnimation();

                }
            }
        });
        return v;
    }

    private void loadImage(){
        mStorageReference2 = FirebaseStorage.getInstance().getReference("userImages/").child(uid + ".png");
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(mStorageReference2)
                .into(mCircleImageView);

    }

    private void loadSongImage(){

        mStorageReference = FirebaseStorage.getInstance().getReference("image/").child(songMp3);
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(mStorageReference)
                .into(mRoundedImageView);
    }

    private void fetchAudioUrlFromFirebase() {

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference("audio/").child(songMp3);

        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    // Download url of file
                    final String url = uri.toString();
                    mMediaPlayer.setDataSource(url);
                    // wait for media player to get prepare
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer = mp;
                            play =1;
                            mMediaPlayer.start();
                        }
                    });
                    mMediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ddd", e.getMessage());
                    }
                });

    }
    private void getFollowers(){

    }


    private void stopMusic(){
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        play = 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMediaPlayer.release();
    }
}
