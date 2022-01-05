package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.R;
import com.sampleapp.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SetupDeviceLocallyFragment extends SetupDeviceBaseFragment {
    static final String DEVICE_ID = "deviceId";
    static final String CANDIDATE_NETWORKS = "candidateNetworks";

    String serializedCandidateNetworks;
    List<WiFiNetwork> deviceCandidateNetworks;
    private Map<String, WiFiNetwork> ssidMap;
    private String selectedNetworkSsid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceId = getArguments().getString(DEVICE_ID);
            serializedCandidateNetworks = getArguments().getString(CANDIDATE_NETWORKS);
            Type type = new TypeToken<List<WiFiNetwork>>() {
            }.getType();
            deviceCandidateNetworks = new Gson().fromJson(serializedCandidateNetworks, type);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        setSpinnerAdapter();

        return view;
    }

    @Override
    public void setFloatingButtonOnClickListener(boolean isHiddenNetworkSetup) {
        final FloatingActionButton layoutFloatingButton = getLayoutFloatingButton();
        if (isHiddenNetworkSetup) {
            layoutFloatingButton.setOnClickListener(hiddenSsidListener);
        } else {
            layoutFloatingButton.setOnClickListener(visibleSsidListener);
        }
    }

    private void setSpinnerAdapter() {
        final List<String> ssidList = new ArrayList<>(getSsidNames(deviceCandidateNetworks));
        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(requireContext(), R.layout.item_spinner, ssidList);

        final Spinner spinnerSsidList = getSsidListSpinner();
        spinnerSsidList.setAdapter(spinnerAdapter);
        spinnerSsidList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String selectedNetwork = spinnerAdapter.getItem(position);
                setSelectedNetworkSsid(selectedNetwork);
                getLayoutFloatingButton().setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing
            }
        });

        preselectPrivateNetwork(Utils.getSsid(getContext()), spinnerAdapter, spinnerSsidList);
    }

    private final View.OnClickListener hiddenSsidListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String hiddenSsid = getHiddenSsid();
            if (hiddenSsid == null || hiddenSsid.isEmpty()) {
                showToast(R.string.enter_ssid, Toast.LENGTH_SHORT);
                return;
            }
            String preSharedKey = getPreSharedKey();
            if (!isOpenSecurityTypeSelected() && preSharedKey.length() < 8) {
                showToast(R.string.incorrect_password_length, Toast.LENGTH_SHORT);
                return;
            }

            final String debugInfo = String.format(
                    "ssid=%s,pwd_length=%s,flags=%s",
                    hiddenSsid,
                    getPreSharedKey().length(),
                    getSelectedSecurityType()
            );
            MobileAppIntelligence.enterStep(
                    StepData.create(
                            StepResult.SUCCESS,
                            "joining_hidden_network",
                            "joining_process_started"
                    ).setDebugInfo(
                            new HashMap<String, String>() {
                                {
                                    put("info", debugInfo);
                                }
                            }
                    )
            );

            final boolean isHiddenNetwork = true;
            hideKeyboardAndShowSendCredentialsFragment(isHiddenNetwork, getSerializedHiddenNetwork(hiddenSsid));
        }
    };

    private final View.OnClickListener visibleSsidListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String selectedNetworkSsid = getSelectedNetworkSsid();
            if (selectedNetworkSsid == null) {
                showToast(R.string.select_network, Toast.LENGTH_SHORT);
                return;
            }
            String preSharedKey = getPreSharedKey();
            if (preSharedKey.length() < 8) {
                showToast(R.string.incorrect_password_length, Toast.LENGTH_SHORT);
                return;
            }

            final String debugInfo = String.format(
                    "ssid=%s,pwd_length=%s,flags=%s",
                    selectedNetworkSsid,
                    preSharedKey.length(),
                    getSelectedSecurityType()
            );
            MobileAppIntelligence.enterStep(
                    StepData.create(
                            StepResult.SUCCESS,
                            "joining_network",
                            "joining_process_started"
                    ).setDebugInfo(
                            new HashMap<String, String>() {
                                {
                                    put("info", debugInfo);
                                }
                            }
                    )
            );

            final boolean isHiddenNetwork = false;
            hideKeyboardAndShowSendCredentialsFragment(isHiddenNetwork, getSerializedSelectedNetwork());
        }
    };

    public Set<String> getSsidNames(List<WiFiNetwork> deviceCandidateNetworks) {
        ssidMap = new HashMap<>();
        for (WiFiNetwork candidateNetwork : deviceCandidateNetworks) {
            final String decodedSsid = candidateNetwork.getDecodedSsid();
            if (!decodedSsid.isEmpty()) {
                ssidMap.put(decodedSsid, candidateNetwork);
            }
        }
        return ssidMap.keySet();
    }

    public void preselectPrivateNetwork(String privateSsid, ArrayAdapter<String> spinnerAdapter, Spinner spinnerSsidList) {
        final int ssidPosition = spinnerAdapter.getPosition(privateSsid);
        if (!privateSsid.isEmpty() && ssidPosition != -1) {
            spinnerSsidList.setSelection(ssidPosition);
            selectedNetworkSsid = spinnerAdapter.getItem(ssidPosition);
        }
    }

    public String getSerializedHiddenNetwork(String hiddenSsid) {
        final WiFiNetwork hiddenNetwork = new WiFiNetwork();
        hiddenNetwork.setFlags(getSelectedSecurityType());
        hiddenNetwork.setSsid(hiddenSsid);
        return new Gson().toJson(hiddenNetwork);
    }

    public String getSerializedSelectedNetwork() {
        final WiFiNetwork selectedNetwork = getSsidMap().get(selectedNetworkSsid);
        return new Gson().toJson(selectedNetwork);
    }

    public String getSelectedNetworkSsid() {
        return selectedNetworkSsid;
    }

    public void setSelectedNetworkSsid(String selectedNetworkSsid) {
        this.selectedNetworkSsid = selectedNetworkSsid;
    }

    public Map<String, WiFiNetwork> getSsidMap() {
        return ssidMap;
    }

    abstract void hideKeyboardAndShowSendCredentialsFragment(boolean isHiddenNetwork, String serializedSelectedNetwork);
}
