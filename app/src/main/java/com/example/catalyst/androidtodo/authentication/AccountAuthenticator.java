package com.example.catalyst.androidtodo.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.activities.AdmissionActivity;
import com.example.catalyst.androidtodo.fragments.LoginFragment;
import com.example.catalyst.androidtodo.network.ApiCaller;

import java.io.IOException;

/**
 * Created by dsloane on 3/18/2016.
 */
public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private String TAG = getClass().getSimpleName();
    private Context context;
    private final String ACCOUNTTYPE;

    public AccountAuthenticator(Context context) {
        super(context);
        this.context = context;
        ACCOUNTTYPE = context.getString(R.string.accountType);
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Log.v(TAG, "editProperties()");
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.v(TAG, "addAccount()");

        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNTTYPE);

        Bundle reply = new Bundle();

        Intent intent = new Intent(context, AdmissionActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        reply.putParcelable(AccountManager.KEY_INTENT, intent);
        return reply;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        Log.v(TAG, "confirmCredentials");
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.v(TAG, "getAuthToken()");
        if (!authTokenType.equals(ACCOUNTTYPE)) {
            Log.v(TAG, "does not match");
            Bundle  result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }
        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        Log.v(TAG, "does match");
        AccountManager am = AccountManager.get(context);
        String password = am.getPassword(account);
        Log.v(TAG, account.name +" " + account.type);

        if (password != null) {
            Log.v(TAG, " password exsists " + account.name +" " + password);
            String authToken = authenticate(account.name, password);
            if (!TextUtils.isEmpty(authToken)) {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNTTYPE);
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                return result;
            }
        }
        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity panel.
        Log.d(TAG,"Login ACtivity");
        Intent intent = new Intent(context, LoginFragment.class);
        //intent.putExtra("username", account.name);
        intent.putExtra(ACCOUNTTYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;

    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.v(TAG, "getAuthTokenLabel()");
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.v(TAG, "updateCredentials()");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        Log.v(TAG, "hasFeatures()");
        return null;
    }
    private String authenticate(String name,String password) {
        Log.v(TAG,String.format("authenticate %S %s",name,password));
        return new ApiCaller().loginUserAndGetToken(name, password);
           // return new UserService().login(new LoginEntity(name, password)).execute().headers().get("X-AUTH-TOKEN");
    }
}
