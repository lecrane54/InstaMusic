package com.kylefebv.audio.audiocut.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kylefebv.audio.audiocut.Activities.MainActivity;
import com.kylefebv.audio.audiocut.Fragments.SingleSongFragment;
import com.kylefebv.audio.audiocut.R;
import com.kylefebv.audio.audiocut.Models.UserSong;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by kyle on 8/8/2017.
 */




    public  class FirebaseAdapter extends RecyclerView.Adapter<FirebaseAdapter.SongItemHolder> {
    public static ArrayList<String> ids = new ArrayList<>();
    public static ArrayList<String> titles = new ArrayList<>();

    public static ArrayList<StorageReference> mp3s = new ArrayList<>();

    private LayoutInflater layoutInflater;
    public static Context context;
    public static StorageReference mStorageReference;
    public static FirebaseStorage mFirebaseStorage;
    public static FirebaseDatabase ref;
    public static String uid;


    public FirebaseAdapter(Context context, FirebaseDatabase ref1, String uid1) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        mFirebaseStorage = FirebaseStorage.getInstance();
        ref = ref1;
        uid = uid1;

    }


    public void FirebaseAdapterAdd() {
        titles.clear();

        ids.clear();
        mp3s.clear();
        ref.getReference("songs").child(uid).orderByValue().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    UserSong song = postSnapshot.getValue(UserSong.class);
                    titles.add(song.getTitle());


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




    //TODO: fix to get the image and song of user
    public void getSongPaths() {

        for (int i = 0; i < ids.size(); i++) {
            // get the corresponding mp3 within ids
            mStorageReference = mFirebaseStorage.getReference("audio/").child(ids.get(i) + ".mp3");

            // mStorageReference = FirebaseStorage.getInstance().getReference(s + ".mp3");

            mp3s.add(mStorageReference);

        }
    }


    /*
        private void loadImage(){
            mStorageReference = FirebaseStorage.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".png");
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(mStorageReference)
                    .into(mCircleImageView);

        }


*/
    @Override
    public SongItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView itemCardView = (CardView) layoutInflater.inflate(R.layout.user_profile_card, parent, false);

        return new SongItemHolder(itemCardView, this);
    }


    @Override
    public void onBindViewHolder(SongItemHolder holder, int position) {
        holder.setItemName(titles.get(position));
        //int value = itemsValue.get(i);
        holder.getImage(position);

    }

    @Override
    public int getItemCount() {
        return titles.size();
    }


    public static class SongItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FirebaseAdapter parent;
        private CardView cardView;
        TextView textItemName;

        CircleImageView imageView;


        public SongItemHolder(CardView cView, FirebaseAdapter parent) {
            super(cView);
            cardView = cView;
            cardView.setOnClickListener(this);
            this.parent = parent;
            textItemName = (TextView) cardView.findViewById(R.id.song_name);

            imageView = (CircleImageView) cardView.findViewById(R.id.song_image);
        }

        public void setItemName(CharSequence name) {
            textItemName.setText(name);
        }

        public CharSequence getItemName() {
            return textItemName.getText();
        }



        public void getImage(int i){
            mStorageReference = FirebaseStorage.getInstance().getReference("image/").child(ids.get(i));
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(mStorageReference)
                    .into(imageView);

        }

        @Override
        public void onClick(View v) {
            if (context instanceof MainActivity) {
                String t = titles.get(getAdapterPosition());
                String u = ids.get(getAdapterPosition());
                String sRef = mp3s.get(getAdapterPosition()).toString();
                Bundle b = new Bundle();
                b.putString("uuid",u);
                b.putString("title",t);
                b.putString("sRef",sRef);
                ((MainActivity) context).switchToSingle(b, new SingleSongFragment());
            }
        }
    }
}
