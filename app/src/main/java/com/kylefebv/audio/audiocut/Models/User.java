package com.kylefebv.audio.audiocut.Models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kyle on 7/6/2017.
 */

public class User {

    private String uid,name,email, fbId;





    public User() {
    }



    public User(String uid, String name, String email, String fbId) {
        this.uid = uid;   // Primary key and key
        this.name = name;
        this.email = email;
        this.fbId = fbId;

    }

    public String getFbId(){
        return fbId;
    }
    public void setFbId(String fbId1){
        this.fbId = fbId1;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name",name);
        result.put("fbId",fbId);
        result.put("uid",uid);
        return result;
    }

}

