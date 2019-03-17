package com.example.valarmorghulis.firebaseauth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

public class User {
    private String uName;
    private String mKey;
    private String uEmail;
    private String uToken;
    FirebaseAuth mAuth;

    public User() {
        //Empty constructor needed
    }

    public User(String token) {
        uName = mAuth.getInstance().getCurrentUser().getDisplayName();
        uEmail = mAuth.getInstance().getCurrentUser().getEmail();
        uToken = token;
    }

    public String getuToken() {
        return uToken;
    }

    public void setuToken(String token) {
        uToken = token;
    }

    public String getName() {
        return uName;
    }

    public void setName(String name) {
        uName = name;
    }

    public String getEmail() {
        return uEmail;
    }

    public void setEmail(String email) {
        uEmail = email;
    }


    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String key) {
        mKey = key;
    }


}
