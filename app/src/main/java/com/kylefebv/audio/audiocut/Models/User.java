package com.kylefebv.audio.audiocut.Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kyle on 7/6/2017.
 */

public class User {

    private String uid,name,email, fbId;
    private HashMap<String,String> following ;
    private HashMap<String,String> followers ;
    public HashMap<String,String> getFollowing() {
        return following;
    }

    public ArrayList<String> setHashToList(ArrayList<String> d ,HashMap<String,String> h){
        d = new ArrayList<>(h.values());
        return d;


    }
    @SuppressWarnings("Since15")
    public void removeFollower(String uid, String follower){
        followers.remove(uid, follower);
    }

    @SuppressWarnings("Since15")
    public void addToFollowers(String uid, String follower){
        followers.put(uid, follower);
    }

    @SuppressWarnings("Since15")
    public void removeFollowing(String uid, String follower){
        following.remove(uid, follower);
    }

    @SuppressWarnings("Since15")
    public void addToFollowing(String uid, String follower){
        following.put(uid, follower);
    }

    public void setFollowing(HashMap<String,String> following) {
        this.following = following;
    }

    public HashMap<String,String> getFollowers() {
        return followers;
    }

    public int followersCount(){
        return followers.size();
    }

    public int followingCount(){
        return following.size();
    }


    public void setFollowers(HashMap<String,String> followers) {
        this.followers = followers;
    }


    public User() {
    }



    public User(String uid, String name, String email, String fbId,HashMap<String,String> followers,HashMap<String,String> following) {
        this.uid = uid;   // Primary key and key
        this.name = name;
        this.email = email;
        this.fbId = fbId;
        this.followers = followers;
        this.following = following;

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
        result.put("following",following);
        result.put("followers",followers);
        return result;
    }

}

