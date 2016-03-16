package com.example.catalyst.androidtodo.util;

import android.os.Build;
import android.provider.ContactsContract;

/**
 * Created by dsloane on 3/16/2016.
 */
public class ContactsConstants {

    public final static String[] PROJECTION =
            {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    Build.VERSION.SDK_INT
                            >= Build.VERSION_CODES.HONEYCOMB ?
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                            ContactsContract.Contacts.DISPLAY_NAME
            };

    public final static int CONTACT_ID_INDEX = 0;

    public final static int LOOKUP_KEY_INDEX = 1;

    public static final int DISPLAY_NAME_INDEX = 2;

    public static final String SEARCH_SELECTION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" :
                    ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
}
