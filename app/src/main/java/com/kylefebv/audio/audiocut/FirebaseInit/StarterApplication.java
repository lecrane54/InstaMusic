
package com.kylefebv.audio.audiocut.FirebaseInit;


import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


public class StarterApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
      FacebookSdk.sdkInitialize(getApplicationContext());
      AppEventsLogger.activateApp(this);
  }
}
