package com.kylefebv.audio.audiocut.Adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kylefebv.audio.audiocut.R;
import com.kylefebv.audio.audiocut.Models.UserSong;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by kyle on 7/25/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    public static ArrayList<String> ids = new ArrayList<>();
    public static ArrayList<String> titles = new ArrayList<>();
    public static ArrayList<String> creators = new ArrayList<>();
    public static ArrayList<StorageReference> mp3s = new ArrayList<>();
    StorageReference mStorageReference;
    FirebaseStorage mFirebaseStorage;
    FirebaseDatabase ref;
    String uid;
    RotateAnimation rotateAnimation;
    int play;
    public static Context context;
    public MediaPlayer mMediaPlayer;
    LayoutInflater layoutInflater;







    public RecyclerAdapter(Context context, FirebaseDatabase ref1, String uid1) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(false);
        mFirebaseStorage = FirebaseStorage.getInstance();
        ref = ref1;
        uid = uid1;
        play = 0;
        RecyclerAdapterAdd();

    }



    public void RecyclerAdapterAdd() {
        titles.clear();
        creators.clear();
        ids.clear();
        mp3s.clear();
        ref.getReference("songs").child(uid).orderByValue().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    UserSong song = postSnapshot.getValue(UserSong.class);
                    titles.add(song.getTitle());
                    creators.add(song.getCreator());

                    ids.add(song.getUuid());
                }
                notifyDataSetChanged();
                getSongPaths();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public  void getSongPaths(){
        for (int i = 0; i < ids.size(); i++) {
            // get the corresponding mp3 within ids
            mStorageReference = mFirebaseStorage.getReference("audio/").child(ids.get(i) + ".mp3");
            mp3s.add(mStorageReference);

        }
    }

    private void loadImage(CircleImageView mCircleImageView){
        mStorageReference = FirebaseStorage.getInstance().getReference("userImages/").child(uid + ".png");
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(mStorageReference)
                .into(mCircleImageView);

    }

    private void loadSongImage(CircleImageView mCircleImageView , int i ){
        mStorageReference = FirebaseStorage.getInstance().getReference("image/").child(ids.get(i));
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(mStorageReference)
                .into(mCircleImageView);

    }
    private void stopMusic(){
        mMediaPlayer.stop();
        mMediaPlayer.reset();

        play = 0;
    }
    private void playSong(String s){

        StorageReference storageRef = mFirebaseStorage.getReference("audio/").child(s);
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





    class ViewHolder extends RecyclerView.ViewHolder{

        public int currentItem;
        RecyclerAdapter parent;
        CardView itemView;

        public TextView itemTitle;
        public TextView itemDetail;
        public CircleImageView profileImage, songImage;


        public ViewHolder(CardView itemView , RecyclerAdapter parent) {
            super(itemView);
            this.parent = parent;
            this.itemView = itemView;
            rotateAnimation = new RotateAnimation(0, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);

            rotateAnimation.setInterpolator(new LinearInterpolator());
            rotateAnimation.setDuration(1200);
            rotateAnimation.setRepeatCount(Animation.INFINITE);

            itemTitle = (TextView)itemView.findViewById(R.id.item_title);
            itemDetail = (TextView)itemView.findViewById(R.id.item_detail);
            profileImage = (CircleImageView)itemView.findViewById(R.id.profImg) ;
            songImage = (CircleImageView)itemView.findViewById(R.id.songImage) ;

            songImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    String s = ids.get(position);
                    if(play == 0){
                        playSong(s);
                        songImage.startAnimation(rotateAnimation);
                    }else{
                        stopMusic();
                        songImage.clearAnimation();
                        playSong(s);
                        songImage.startAnimation(rotateAnimation);
                    }

                }
            });

        }
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        CardView itemCardView = (CardView) layoutInflater.inflate(R.layout.cardview, viewGroup, false);
        return new ViewHolder(itemCardView, this);

    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.itemTitle.setText(titles.get(i));
        viewHolder.itemDetail.setText(creators.get(i));
        loadImage(viewHolder.profileImage);
        loadSongImage(viewHolder.songImage,i);
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }
}
