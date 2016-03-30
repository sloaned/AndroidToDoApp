package com.example.catalyst.androidtodo.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.example.catalyst.androidtodo.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by dsloane on 3/22/2016.
 */
public class MapPickerFragment extends DialogFragment implements OnMapReadyCallback {

    private static final String TAG = MapPickerFragment.class.getSimpleName();

    private GoogleMap googleMap;

    public MapPickerFragment() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View mapView = inflater.inflate(R.layout.mappicker, null);

        builder.setView(mapView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "clicked ok");
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "clicked cancel");
            }
        });

        return builder.create();

    }


    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }

    public static MapPickerFragment newInstance() {
        MapPickerFragment fragment = new MapPickerFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }





}
