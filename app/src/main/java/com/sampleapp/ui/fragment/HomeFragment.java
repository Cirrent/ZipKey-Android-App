package com.sampleapp.ui.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.OnboardingType;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.google.android.material.navigation.NavigationView;
import com.sampleapp.CirrentApplication;
import com.sampleapp.MaiHelper;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.Utils;
import com.sampleapp.ui.activity.HomeActivity;


public class HomeFragment extends BaseFragment {
    private static boolean triedToDisconnectFromSoftAp = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        changeActionBarState(false, false, getString(R.string.home_title));
        initViews(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        checkIfDeviceIsConnectedToSoftAp();

        Menu sideMenu = ((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu();
        sideMenu.findItem(R.id.nav_products).setChecked(true);
    }

    private void checkIfDeviceIsConnectedToSoftAp() {
        final String softApSsid = Prefs.SOFT_AP_SSID.getValue();
        final String currentSsid = Utils.getSsid(getContext());
        if (currentSsid.equals(softApSsid)) {
            if (!triedToDisconnectFromSoftAp) {
                triedToDisconnectFromSoftAp = true;
                showFragment(DisconnectFromSoftApLoadingFragment.newInstance(softApSsid), false);
            } else {
                Toast.makeText(
                        CirrentApplication.getAppContext(),
                        "Failed to disconnect from " + softApSsid + " network",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void initViews(View view) {
        view.findViewById(R.id.floating_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int fineLocationPermissionStatus =
                        ActivityCompat.checkSelfPermission(
                                CirrentApplication.getAppContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION
                        );
                if (fineLocationPermissionStatus != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            requireActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            HomeActivity.LOCATION_REQUEST_CODE
                    );
                } else {
                    new MaiHelper().initializeMobileAppIntelligence();
                    startLocalOnboardingProcess();
                }
            }
        });
    }

    private void startLocalOnboardingProcess() {
        if (Prefs.IS_SOFTAP_ONBOARDING_ACTIVATED.getValue()) {
            connectViaSoftAp();
        } else {
            startBluetoothOnboarding();
        }
    }

    private void connectViaSoftAp() {
        triedToDisconnectFromSoftAp = false;
        MobileAppIntelligence.startOnboarding(OnboardingType.SOFTAP);
        getAnalyticsService().logEvent("real_flow", "soft_ap");
        saveCurrentNetworkData();

        String ssid = Prefs.SOFT_AP_SSID.getValue();
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.SUCCESS,
                        "connecting_via_softap",
                        "softap_onboarding_started"
                )
        );
        showFragment(ConnectViaSoftApLoadingFragment.newInstance(ssid), false);
    }

    private void startBluetoothOnboarding() {
        MobileAppIntelligence.startOnboarding(OnboardingType.BLE);
        getAnalyticsService().logEvent("real_flow", "ble");
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.SUCCESS,
                        "connecting_via_ble",
                        "ble_onboarding_started"
                )
        );
        showFragment(new ConnectViaBluetoothLoadingFragment(), false);
    }

    private void saveCurrentNetworkData() {
        Prefs.PRIVATE_SSID.setValue(Utils.getSsid(CirrentApplication.getAppContext()));
        int wifiNetworkId = Utils.getWifiNetworkId(CirrentApplication.getAppContext());
        Prefs.WIFI_NETWORK_ID.setValue(wifiNetworkId);
    }
}
