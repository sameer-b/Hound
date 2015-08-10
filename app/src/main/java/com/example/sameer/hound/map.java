package com.example.sameer.hound;

/**
 * Created by Sameer on 8/9/2015.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class map extends Fragment {
    GoogleMap map;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.map, container, false);
        SupportMapFragment supportMap = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        map = supportMap.getMap();
        return v;
    }

}
