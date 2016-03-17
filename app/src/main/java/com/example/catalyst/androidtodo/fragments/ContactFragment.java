package com.example.catalyst.androidtodo.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.catalyst.androidtodo.activities.HomeActivity;
import com.example.catalyst.androidtodo.adapters.ContactAdapter;

import java.util.ArrayList;

/**
 * Created by dsloane on 3/17/2016.
 */
public class ContactFragment extends DialogFragment {

    private final String TAG = ContactFragment.class.getSimpleName();
    private static ArrayList<String> contacts = new ArrayList<String>();
    private static int participantNumber;
    private ContactAdapter adapter;
    private static ContactClickListener callback;

    public ContactFragment() {}

    public interface ContactClickListener {
        public void assignParticipant(String name, int id);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        try {
            callback = (ContactClickListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        ListView contactList = new ListView(getActivity());
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, contacts);
        adapter = new ContactAdapter(getActivity(), contacts);

        contactList.setAdapter(adapter);

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) adapter.getItem(position);

                Log.d(TAG, "Target fragment = " + getTargetFragment());

                callback.assignParticipant(contacts.get(position), participantNumber);
                ContactFragment.this.dismiss();
            }
        });




        builder.setView(contactList);
        builder.setTitle("Contacts");

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }


    public static ContactFragment newInstance(ArrayList<String> contactList, int participant) {
        contacts = contactList;
        participantNumber = participant;

        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public interface onFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
