package com.kylefebv.audio.audiocut.Fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dd.processbutton.iml.GenerateProcessButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kylefebv.audio.audiocut.R;
import com.kylefebv.audio.audiocut.Models.UserSong;
import com.pavlospt.rxfile.RxFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddSongFragment extends Fragment{

    private EditText mTitle;
    private ImageView mImageView , soundImageView;
    private GenerateProcessButton mButton;
    FirebaseStorage mFirebaseStorage;
    StorageReference storageRef;
    FirebaseDatabase database;
    DatabaseReference mDatabaseReference;
    String uid,title,creator,fbId;
    Uri uri;
    UserSong song;
    String mRandom;
    File mFile;
    String filepath, picFilePath;
    ProgressDialog mProgressDialog;



    public AddSongFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_add_song, container, false);
        mTitle = (EditText)v.findViewById(R.id.title_edit);
        mImageView = (ImageView)v.findViewById(R.id.image);
        mButton = (GenerateProcessButton) v.findViewById(R.id.button);



        // / icon
        soundImageView = (ImageView)v.findViewById(R.id.sound);

        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseStorage = FirebaseStorage.getInstance();
        uid = u.getUid();
        RxFile.setLoggingEnabled(true);
        database = FirebaseDatabase.getInstance();
        mDatabaseReference = database.getReference("songs").child(uid);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle("Uploading that snazzy tune");
        mProgressDialog.setMessage("Be patient");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(false);

        creator = u.getDisplayName();
        song = new UserSong();
        mRandom = song.generateRandomUID();

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i,2);
            }
        });

        soundImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = mTitle.getText().toString();
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);

                i.setType("audio/*");
                startActivityForResult(i,1);
            }
        });


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = mTitle.getText().toString().trim();

                if(!title.equals("") || filepath.equals(null) || picFilePath.equals(null)){

                    uploadSong(filepath);
                }
                else{
                    Toasty.error(getApplicationContext(),"Check to see if you have a title, image and song!",Toast.LENGTH_LONG).show();
                }


            }
        });

        return v;
    }


    private void cacheFileFromDrive(Uri uri, final String fileName) {
        RxFile.createFileFromUri(getActivity(),uri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<File>() {
                    @Override
                    public void onCompleted() {
                        soundImageView.setImageResource(R.drawable.check);

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(File file) {
                        filepath = file.getAbsolutePath();
                    }
                });

    }

    private void getPictureFile(Uri uri, final String fileName) {
        RxFile.createFileFromUri(getActivity(),uri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<File>() {
                    @Override
                    public void onCompleted() {
                        uploadImage(picFilePath);
                        Bitmap b = BitmapFactory.decodeFile(picFilePath);
                        mImageView.setImageBitmap(b);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(File file) {
                        picFilePath = file.getAbsolutePath();
                    }
                });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK){
            DocumentFile file = DocumentFile.fromSingleUri(getActivity(),data.getData());

            if(file.getName().endsWith(".mp3") || file.getName().endsWith(".m4a")) {
                if(!mTitle.getText().toString().equals("")){

                    cacheFileFromDrive(file.getUri(), file.getName());
                }else{
                    Toasty.error(getActivity(),"Enter a title",Toast.LENGTH_LONG).show();
                }
            }else{
                Toasty.error(getActivity(),"Only upload mp3s",Toast.LENGTH_LONG).show();
            }

        }
        if(requestCode == 2 && resultCode == RESULT_OK){
            DocumentFile file = DocumentFile.fromSingleUri(getActivity(),data.getData());

            if(file.getName().endsWith(".jpg")|| file.getName().endsWith(".png")){
                getPictureFile(file.getUri(),file.getName());
            }
        }


    }

    private void uploadSong(String path){
        Uri file = Uri.fromFile(new File(path));
        storageRef = mFirebaseStorage.getReference("audio/").child(mRandom);
// Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("audio/mp3")
                .build();

        UploadTask uploadTask = storageRef.putFile(file, metadata);

// Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
               // mProgressDialog.show();
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
               // mProgressDialog.setProgress((int)progress);
                mButton.setProgress((int)progress);

            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                setValues(title,creator);
                // Handle successful uploads on complete
                Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                resetValues();
               // mProgressDialog.hide();

                Toasty.success(getActivity(),"Your song was uploaded",Toast.LENGTH_LONG).show();


            }
        });
    }

    private void resetValues(){
        song = new UserSong();
        mRandom = song.generateRandomUID();
        mTitle.setText("");
        picFilePath = null;
        filepath = null;
        mImageView.setImageResource(R.drawable.camera);
        soundImageView.setImageResource(R.drawable.isound);
    }

    private void setValues(String title, String creator){

        song.setCreator(creator);
        song.setTitle(title);
        song.setDate();
        song.setMilli();
        song.setUuid(mRandom);
        savePlace();
    }


    private void savePlace() {

        String key = mDatabaseReference.child(uid).push().getKey();

        Map<String, Object> postValues = song.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(mRandom, postValues);
        mDatabaseReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();

            }
        });


    }

    private void uploadImage(String path){
        Uri file = Uri.fromFile(new File(path));
        storageRef = mFirebaseStorage.getReference("image/").child(mRandom);
// Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/png")
                .build();

        UploadTask uploadTask = storageRef.putFile(file, metadata);

// Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();


            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
                Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();


            }
        });
    }





}
