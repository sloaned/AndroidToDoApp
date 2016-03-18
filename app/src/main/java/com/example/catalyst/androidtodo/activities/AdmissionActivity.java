package com.example.catalyst.androidtodo.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.authentication.AccountAuthenticator;
import com.example.catalyst.androidtodo.event.TokenEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.ButterKnife;

/**
 * Created by dsloane on 3/18/2016.
 */
public class AdmissionActivity extends AccountAuthenticatorActivity {

    private final String TAG = getClass().getSimpleName();
    private String mEmail;
    private String mPassword;
    private String mToken;

    @Subscribe
    public void onEvent(TokenEvent event) {
        Log.i(TAG, "Token received");
        mToken = event.getToken();
        if (event.isHasCredentials()) {
            mEmail = event.getEmail();
            mPassword = event.getPassword();
        }
        handleToken();
    }

    @NonNull
    private Account addAccount(String email, String password) {
        Account account = new Account(email, getString(R.string.accountType));
        AccountManager am = AccountManager.get(this);

        boolean accountCreated = am.addAccountExplicitly(account, password, null);
        Log.v(TAG, "account was created: " + accountCreated);
        ContentResolver.setSyncAutomatically(account, getString(R.string.contentAuthority), true);
        return account;
    }

    private void handleToken() {
        Log.v(TAG, "Token not null");
        Account account = addAccount(mEmail, mPassword);

        setUpResult();
    }

    private void setUpResult() {
        Intent result = new Intent();
        result.putExtra(AccountManager.KEY_ACCOUNT_NAME, mEmail);
        result.putExtra(AccountManager.KEY_ACCOUNT_TYPE, getString(R.string.accountType));
        result.putExtra(AccountManager.KEY_AUTHTOKEN, mToken);

        setAccountAuthenticatorResult(result.getExtras());
        setResult(Activity.RESULT_OK, result);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }

}
