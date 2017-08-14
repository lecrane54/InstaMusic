package com.kylefebv.audio.audiocut.Models;

import android.text.format.DateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by kyle on 7/25/2017.
 */

public class UserSong {

String title, creator,uuid, date;

    public Long getMilli() {
        return milli;
    }

    public void setMilli() {
        this.milli = System.currentTimeMillis();
    }

    Long milli;

    public String getDate() {
        return date;
    }

    public void setDate() {

        String dateString = DateFormat.format("dd.MM.yyyy HH:mm:ss ", new Date(System.currentTimeMillis())).toString();
        this.date = dateString;
    }




    public UserSong(){

    }
    public UserSong(String title, String creator,String uuid, String date, Long milli){
       this.uuid = uuid;
        this.title = title;
        this.creator = creator;
        this.date = date;
        this.milli = milli;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        title = value;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String value) {
        creator = value;
    }


    public String generateRandomUID(){
        uuid = UUID.randomUUID().toString();
        return uuid;
    }
    public String getUuid(){
        return uuid;
    }
    public void setUuid(String uid) {
        this.uuid = uid;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("creator", creator);
        result.put("title", title);
        result.put("date",date);
        result.put("milli",milli);
        return result;
    }
}
