package com.sampleapp.ui.fragment;

import android.os.Bundle;

public class SetupDeviceViaSoftApFragment extends SetupDeviceLocallyFragment {

    public static SetupDeviceViaSoftApFragment newInstance(String deviceId, String serializedCandidateNetworks) {
        SetupDeviceViaSoftApFragment fragment = new SetupDeviceViaSoftApFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_ID, deviceId);
        args.putString(CANDIDATE_NETWORKS, serializedCandidateNetworks);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void hideKeyboardAndShowSendCredentialsFragment(boolean isHiddenNetwork,
                                                    String serializedSelectedNetwork) {
        showFragment(SendCredentialsViaSoftApFragment.newInstance(
                deviceId,
                serializedSelectedNetwork,
                isHiddenNetwork,
                getPreSharedKey(),
                serializedCandidateNetworks
        ), false);
    }
}
