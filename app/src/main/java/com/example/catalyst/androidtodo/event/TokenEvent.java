package com.example.catalyst.androidtodo.event;

/**
 * Created by dsloane on 3/18/2016.
 */
public class TokenEvent {

    private final String mToken;
    private final String mEmail;
    private final String mPassword;
    private final boolean hasCredentials;

    public TokenEvent(String token) {
        mToken = token;
        mEmail = null;
        mPassword = null;
        hasCredentials = false;
    }

    public TokenEvent(String token, String email, String password) {
        mToken = token;
        mEmail = email;
        mPassword = password;
        hasCredentials = true;
    }

    public String getToken() { return mToken; }

    public String getEmail() { return mEmail; }

    public String getPassword() { return mPassword; }

    public boolean isHasCredentials() { return hasCredentials; }


}
