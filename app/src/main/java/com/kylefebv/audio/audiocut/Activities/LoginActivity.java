package com.kylefebv.audio.audiocut.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kylefebv.audio.audiocut.Models.User;
import com.kylefebv.audio.audiocut.R;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    LoginButton loginButton;
    CallbackManager mCallbackManager;
    String TAG = "Hey";
    String name, fbid, email, picUrl;
    String uid;
    FirebaseDatabase database;
    DatabaseReference userRef;
    public static byte[] data;
    public Bitmap bitmap1;
   User mUser = new User();
    FirebaseUser mFirebaseUser;
    FirebaseStorage storage;
    StorageReference storageRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        loginButton = (LoginButton) findViewById(R.id.login);



        mAuth = FirebaseAuth.getInstance();
        initDatabase();

       checkUser();


        mCallbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {


                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.e("response: ", response + "");
                                try {
                                    fbid = object.getString("id").toString();
                                    email = object.getString("email").toString();
                                    name = object.getString("name").toString();
                                    //  mUser.setPicUrl("https://graph.facebook.com/" + loginResult.getAccessToken().getUserId() + "/picture?type=large";
                                    JSONObject picture = object.getJSONObject("picture"); // get user image
                                    JSONObject data = picture.getJSONObject("data"); // get data from piture
                                    picUrl = data.getString("url");


                                    // checkFBLogin(id, email, name, profilePicUrl);
                                    new ProfilePhotoAsync(picUrl).execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }

                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,picture.type(large)");
                request.setParameters(parameters);
                request.executeAsync();

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });


        mAuthListener = new FirebaseAuth.AuthStateListener() {


            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mFirebaseUser = firebaseAuth.getCurrentUser();

                if (mFirebaseUser != null) {
                    uid = mFirebaseUser.getUid();

                } else {
                    Toast.makeText(LoginActivity.this, "something went wrong", Toast.LENGTH_LONG).show();
                }


            }
        };

    }


    private void checkUser() {
        FirebaseUser u = mAuth.getCurrentUser();
        if (u != null) {
            nextActivity();
        }

    }

    private void nextActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void initDatabase() {

        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users");
        storage = FirebaseStorage.getInstance();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                uid = mAuth.getCurrentUser().getUid();
                createUser();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Authentication error",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUser() {
        mUser.setFbId(fbid);
        mUser.setName(name);
        mUser.setEmail(email);
        mUser.setUid(uid);

        uploadFile(bitmap1,uid);

        userRef.child(uid).setValue(mUser);
    }


    private void uploadFile(Bitmap bitmap, String uid) {
        storageRef = storage.getReference("userImages/").child(uid + ".png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toasty.error(getApplicationContext(),"Something went wrong when uploading photo",Toast.LENGTH_LONG).show();
                Log.d("ddd",exception.getLocalizedMessage().toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                nextActivity();
            }
        });

    }

    class ProfilePhotoAsync extends AsyncTask<String, String, String> {
        public Bitmap bitmap;
        String url;

        public ProfilePhotoAsync(String url) {
            this.url = url;
        }

        @Override
        protected String doInBackground(String... params) {
            bitmap = DownloadImageBitmap(url);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            bitmap1 = bitmap;
        }
    }

    public static Bitmap DownloadImageBitmap(String url) {
        Bitmap bm = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = 1;
            options.inScaled = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis, null, options);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
            data = stream.toByteArray();
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("IMAGE", "Error getting bitmap", e);
        }
        return bm;
    }

}
