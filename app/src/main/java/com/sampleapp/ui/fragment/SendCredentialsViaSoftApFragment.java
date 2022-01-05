package com.sampleapp.ui.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.cirrent.cirrentsdk.service.SoftApService;
import com.cirrent.networkintelligencedata.onboarding.EndData;
import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.CirrentApplication;
import com.sampleapp.Prefs;
import com.sampleapp.R;

import java.util.HashMap;

public class SendCredentialsViaSoftApFragment extends SendCredentialsBaseFragment implements SoftApService.SoftApCredentialsSenderCallback {
    private static final String CANDIDATE_NETWORKS = "candidateNetworks";

    private String serializedCandidateNetworks;

    public static SendCredentialsViaSoftApFragment newInstance(String deviceId,
                                                               String serializedSelectedNetwork,
                                                               boolean isHiddenNetwork,
                                                               String preSharedKey,
                                                               String serializedCandidateNetworks) {
        SendCredentialsViaSoftApFragment fragment = new SendCredentialsViaSoftApFragment();
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
            deviceId = arguments.getString(DEVICE_ID, "");
            isHiddenNetwork = arguments.getBoolean(HIDDEN_NETWORK);
            preSharedKey = arguments.getString(PRE_SHARED_KEY);
            serializedCandidateNetworks = getArguments().getString(CANDIDATE_NETWORKS);
            deserializeSelectedNetwork(arguments.getString(SELECTED_NETWORK));
        }
    }

    private void deserializeSelectedNetwork(String serializedSelectedNetwork) {
        java.lang.reflect.Type type = new TypeToken<WiFiNetwork>() {
        }.getType();
        selectedNetwork = new Gson().fromJson(serializedSelectedNetwork, type);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        putPrivateCredentialsViaSoftAp();

        return view;
    }

    private void putPrivateCredentialsViaSoftAp() {
        final int priority = 200;
        final String softApSsid = Prefs.SOFT_AP_SSID.getValue();

        //----- SDK call ------------
        SoftApService
                .getSoftApService()
                .setProgressView(progressView.withText(R.string.sending_via_soft_ap))
                .setSoftApDeviceStatusTimings(5, 10)
                .setSoftApDisconnectTimings(7, 5)
                .setSoftApPort(80)
                .putPrivateCredentialsViaSoftAp(
                        isHiddenNetwork,
                        Prefs.WIFI_NETWORK_ID.getValue(),
                        priority,
                        CirrentApplication.getAppContext(),
                        softApSsid,
                        selectedNetwork,
                        preSharedKey,
                        this,
                        new CommonErrorCallback() {
                            @Override
                            public void onFailure(final CirrentException e) {
                                Log.e("CRED_SENDER", e.getMessage() + ". Code:" + e.getErrorCode());
                                showToast(R.string.joining_failed_try_again, Toast.LENGTH_SHORT);
                                MobileAppIntelligence.endOnboarding(
                                        EndData.createFailure("joining_failed").setDebugInfo(
                                                new HashMap<String, String>() {
                                                    {
                                                        put("error", e.getMessage());
                                                    }
                                                }
                                        )
                                );
                                showFragment(new HomeFragment(), false);
                            }
                        });
        //---------------------------
    }

    @Override
    public void onCredentialsSent() {
        progressView.withText(getString(R.string.bt_checking_status));
    }

    @Override
    public void onReturnedToNetworkWithInternet(boolean isDeviceConnectedToNetwork) {
        if (isDeviceConnectedToNetwork) {
            MobileAppIntelligence.enterStep(
                    StepData.create(
                            StepResult.SUCCESS,
                            "finishing",
                            "successfully_joined_network")
            );
            showFragment(
                    SuccessFragment.newInstance(
                            deviceId,
                            selectedNetwork.getDecodedSsid()
                    ),
                    false
            );
        } else {
            showToast(R.string.joining_failed_try_again, Toast.LENGTH_SHORT);
            MobileAppIntelligence.endOnboarding(EndData.createFailure("failed_to_connect_device_to_network"));
            showFragment(new HomeFragment(), false);
        }
    }

    @Override
    public void onNetworkJoiningFailed() {
        showToast(R.string.joining_failed_try_again, Toast.LENGTH_SHORT);
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.FAILURE,
                        "enter_creds",
                        "couldnt_join_private_network"
                )
        );
        showFragment(
                SetupDeviceViaSoftApFragment.newInstance(
                        deviceId,
                        serializedCandidateNetworks
                ),
                false
        );
    }

    @Override
    public void onError(SoftApService.SoftApCredentialsSenderError error) {
        switch (error) {
            case INCORRECT_PRIORITY_VALUE_USED:
                endOnboarding("incorrect_priority_value", R.string.incorrect_priority_value);
                break;
            case INVALID_SCD_PUBLIC_KEY_USED:
                endOnboarding(
                        "incorrect_scdPublicKey_value",
                        R.string.invalid_scd_public_key_reboot_device_and_try_again
                );
                break;
            case FAILED_TO_RETURN_TO_PRIVATE_NETWORK:
                endOnboarding(
                        "failed_to_return_to_internet",
                        R.string.failed_to_return_to_network
                );
                break;
        }
    }
}
