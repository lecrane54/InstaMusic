package com.kylefebv.audio.audiocut.Activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kylefebv.audio.audiocut.Fragments.AddSongFragment;
import com.kylefebv.audio.audiocut.Fragments.OtherUserFragment;
import com.kylefebv.audio.audiocut.R;
import com.kylefebv.audio.audiocut.Fragments.UserFeedFragment;
import com.kylefebv.audio.audiocut.Fragments.UserProfileFragment;


public class MainActivity extends AppCompatActivity {

    private String uid,name;
    NavigationTabStrip navigationTabStrip;
    FragmentManager manager;
    final Fragment fragment = new UserFeedFragment();
    final Fragment fragment1 = new UserProfileFragment();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = firebaseUser.getUid();
        name = firebaseUser.getDisplayName();
        navigationTabStrip = (NavigationTabStrip) findViewById(R.id.nts);
        navigationTabStrip.setTabIndex(0);
        manager = getSupportFragmentManager();



        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.commit();


        navigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {

                if(index == 0){
                    FragmentTransaction ft = manager.beginTransaction();
                    ft.replace(R.id.container, fragment);
                    ft.commit();
                }
                if(index == 1){
                    Log.d("ddd","dude");
                }
                if(index == 2){
                    FragmentTransaction ft = manager.beginTransaction();
                    ft.replace(R.id.container,new AddSongFragment());
                    ft.commit();
                }
                if (index == 3) {
                    Bundle b = new Bundle();
                    b.putString("uid",uid);
                    b.putString("name",name);
                    switchToSingle(b,new UserProfileFragment());

                }
            }

            @Override
            public void onEndTabSelected(String title, int index) {

            }
        });



    }



   public void switchToSingle(Bundle b , Fragment f){

       FragmentTransaction ft = manager.beginTransaction();
       f.setArguments(b);
       ft.replace(R.id.container,f);
       ft.commit();

   }

   public void switchToProfile(Bundle b){
       Fragment f = new OtherUserFragment();
       f.setArguments(b);
       FragmentTransaction ft = manager.beginTransaction();
       ft.replace(R.id.container,f);
       ft.commit();
   }
}

