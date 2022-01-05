package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sampleapp.R;
import com.sampleapp.Utils;

public class LocalConnectionLoadingFragment extends BaseFullScreenFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_find_device_loading, container, false);
        Utils.startConstantVerticalViewAnimation(view.findViewById(R.id.logo));

        changeActionBarState(true, false, "");

        return view;
    }
}
