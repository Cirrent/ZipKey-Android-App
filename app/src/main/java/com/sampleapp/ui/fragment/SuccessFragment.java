package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.sampleapp.R;

public class SuccessFragment extends BaseFragment {
    private static final String DEVICE_ID = "deviceId";
    private static final String NETWORK_SSID = "networkSsid";

    private String deviceId;
    private String networkSsid;
    private TextView textSuccessInfoDeviceName;
    private TextView textSuccessInfoNetworkName;

    public static SuccessFragment newInstance(String deviceId, String networkSsid) {
        SuccessFragment fragment = new SuccessFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_ID, deviceId);
        args.putString(NETWORK_SSID, networkSsid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            deviceId = arguments.getString(DEVICE_ID);
            networkSsid = arguments.getString(NETWORK_SSID);
        }

        MobileAppIntelligence.endOnboarding();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_success, container, false);

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        changeActionBarState(false, true, "");
        initViews(view);
        fillSuccessInfo();

        return view;
    }

    private void initViews(View view) {
        textSuccessInfoDeviceName = (TextView) view.findViewById(R.id.text_device_name);
        textSuccessInfoNetworkName = (TextView) view.findViewById(R.id.text_network_name);

        view.findViewById(R.id.floating_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new HomeFragment(), false);
            }
        });
    }

    private void fillSuccessInfo() {
        String notEmptyDeviceId = (deviceId == null || deviceId.isEmpty()) ? "Your device" : deviceId;
        textSuccessInfoDeviceName.setText(notEmptyDeviceId);
        textSuccessInfoNetworkName.setText(networkSsid);
    }
}
