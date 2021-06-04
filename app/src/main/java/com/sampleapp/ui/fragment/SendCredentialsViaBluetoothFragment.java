package com.sampleapp.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.cirrent.cirrentsdk.service.BluetoothService;
import com.cirrent.networkintelligencedata.onboarding.EndData;
import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.CirrentApplication;
import com.sampleapp.R;

import java.lang.reflect.Type;

public class SendCredentialsViaBluetoothFragment extends SendCredentialsBaseFragment implements BluetoothService.BluetoothCredentialsSenderCallback {
    private static final String CANDIDATE_NETWORKS = "candidateNetworks";

    private String serializedCandidateNetworks;

    public static SendCredentialsViaBluetoothFragment newInstance(String deviceId,
                                                                  String serializedSelectedNetwork,
                                                                  boolean isHiddenNetwork,
                                                                  String preSharedKey,
                                                                  String serializedCandidateNetworks) {
        SendCredentialsViaBluetoothFragment fragment = new SendCredentialsViaBluetoothFragment();
        Bundle args = new Bundle();
        args.putBoolean(HIDDEN_NETWORK, isHiddenNetwork);
        args.putString(DEVICE_ID, deviceId);
        args.putString(SELECTED_NETWORK, serializedSelectedNetwork);
        args.putString(PRE_SHARED_KEY, preSharedKey);
        args.putString(CANDIDATE_NETWORKS, serializedCandidateNetworks);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            deviceId = arguments.getString(DEVICE_ID);
            isHiddenNetwork = arguments.getBoolean(HIDDEN_NETWORK);
            preSharedKey = arguments.getString(PRE_SHARED_KEY);
            serializedCandidateNetworks = getArguments().getString(CANDIDATE_NETWORKS);
            deserializeSelectedNetwork(arguments.getString(SELECTED_NETWORK));
        }
    }

    private void deserializeSelectedNetwork(String serializedSelectedNetwork) {
        Type type = new TypeToken<WiFiNetwork>() {
        }.getType();
        selectedNetwork = new Gson().fromJson(serializedSelectedNetwork, type);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        putPrivateCredentialsViaBluetooth();

        return view;
    }

    private void putPrivateCredentialsViaBluetooth() {
        final int priority = 200;
        //----- SDK call ------------
        progressView.withText(getString(R.string.bt_sending_creds));
        BluetoothService
                .getBluetoothService()
                .putPrivateCredentialsViaBluetooth(
                        isHiddenNetwork,
                        priority,
                        CirrentApplication.getAppContext(),
                        selectedNetwork,
                        preSharedKey,
                        this
                );
        //---------------------------
    }

    @Override
    public void onOperationTimeLimitExceeded() {
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.FAILURE,
                        "enter_creds",
                        "time_limit_exceeded")
        );
        showToast(R.string.time_limit_exceeded, Toast.LENGTH_LONG);
        showFragment(
                SetupDeviceViaBluetoothFragment.newInstance(
                        deviceId,
                        serializedCandidateNetworks
                ),
                false
        );
    }

    @Override
    public void onFailedToGetNetworkStatus() {
        //Cirrent Agent specific behaviour
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.SUCCESS,
                        "finishing",
                        "failed_to_get_network_status")
        );
        showSuccessFragment();
    }

    @Override
    public void onConnectionIsNotEstablished() {
        MobileAppIntelligence.endOnboarding(EndData.createFailure("connection_is_not_established"));
        showToast(R.string.not_connected, Toast.LENGTH_LONG);
        showFragment(new HomeFragment(), false);
    }

    @Override
    public void onCredentialsSent() {
        progressView.withText(getString(R.string.bt_checking_status));
    }

    @Override
    public void onConnectedToPrivateNetwork() {
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.SUCCESS,
                        "finishing",
                        "successfully_joined_network")
        );
        showSuccessFragment();
    }

    @Override
    public void onNetworkJoiningFailed() {
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.FAILURE,
                        "enter_creds",
                        "couldnt_join_private_network"
                )
        );
        showToast(R.string.bt_joining_failed, Toast.LENGTH_LONG);
        showFragment(
                SetupDeviceViaBluetoothFragment.newInstance(
                        deviceId,
                        serializedCandidateNetworks
                ),
                false
        );
    }

    @Override
    public void onIncorrectPriorityValueUsed() {
        MobileAppIntelligence.endOnboarding(EndData.createFailure("incorrect_priority_value"));
        showToast(R.string.incorrect_priority_value, Toast.LENGTH_LONG);
        showFragment(new HomeFragment(), false);
    }

    private void showSuccessFragment() {
        showFragment(
                SuccessFragment.newInstance(
                        deviceId,
                        selectedNetwork.getDecodedSsid()
                ),
                false
        );
    }
}
