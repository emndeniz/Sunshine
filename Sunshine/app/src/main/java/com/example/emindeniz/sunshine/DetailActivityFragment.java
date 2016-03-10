package com.example.emindeniz.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }
    private TextView forecastTextview;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        forecastTextview = (TextView) rootView.findViewById(R.id.detailText);

        Intent mainIntent = getActivity().getIntent();

        if (mainIntent !=null && mainIntent.hasExtra(Intent.EXTRA_TEXT)){
            String forecast = mainIntent.getStringExtra(Intent.EXTRA_TEXT);
            forecastTextview.setText(forecast);
        }


        return rootView;
    }
}
