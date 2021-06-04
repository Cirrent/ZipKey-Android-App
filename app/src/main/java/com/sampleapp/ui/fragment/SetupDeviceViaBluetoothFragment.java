package com.sampleapp.ui.fragment;

import android.os.Bundle;

import com.sampleapp.Utils;

public class SetupDeviceViaBluetoothFragment extends SetupDeviceLocallyFragment {

    public static SetupDeviceViaBluetoothFragment newInstance(String deviceId, String serializedCandidateNetworks) {
        SetupDeviceViaBluetoothFragment fragment = new SetupDeviceViaBluetoothFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_ID, deviceId);
        args.putString(CANDIDATE_NETWORKS, serializedCandidateNetworks);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void hideKeyboardAndShowSendCredentialsFragment(boolean isHiddenNetwork, String serializedSelectedNetwork) {
        Utils.hideKeyboard(requireActivity());
        showFragment(SendCredentialsViaBluetoothFragment.newInstance(
                deviceId,
                serializedSelectedNetwork,
                isHiddenNetwork,
                getPreSharedKey(),
                serializedCandidateNetworks), false);
    }
}
