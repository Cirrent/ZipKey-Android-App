package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cirrent.cirrentsdk.service.SoftApService;
import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.sampleapp.CirrentApplication;
import com.sampleapp.MaiConstants;
import com.sampleapp.Prefs;
import com.sampleapp.R;

public class DisconnectFromSoftApLoadingFragment extends LocalConnectionLoadingFragment {
    private static final String SOFT_AP_SSID = "softApSsid";
    private String softApSsid;

    public static DisconnectFromSoftApLoadingFragment newInstance(String softApSsid) {
        DisconnectFromSoftApLoadingFragment fragment = new DisconnectFromSoftApLoadingFragment();
        Bundle args = new Bundle();
        args.putString(SOFT_AP_SSID, softApSsid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            softApSsid = getArguments().getString(SOFT_AP_SSID);
        }

        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.SUCCESS,
                        "disconnecting_from_softap",
                        "still_connected_to_soft_ap"
                )
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        setStatusText(view);
        leaveSoftApNetwork();

        return view;
    }

    private void leaveSoftApNetwork() {
        //----- SDK call ------------
        SoftApService
                .getSoftApService()
                .setSoftApDisconnectTimings(7, 5)
                .leaveSoftApNetwork(CirrentApplication.getAppContext(), softApSsid, Prefs.WIFI_NETWORK_ID.getValue(), new SoftApService.InternetConnectionCallback() {
                    @Override
                    public void onReturnedToNetworkWithInternet() {
                        MobileAppIntelligence.enterStep(
                                StepData.create(
                                        StepResult.SUCCESS,
                                        MaiConstants.WAITING_ONBOARDING,
                                        "successfully_disconnected_from_softap"
                                )
                        );
                        showFragment(new HomeFragment(), false);
                    }

                    @Override
                    public void onFailedToReturnToNetworkWithInternet() {
                        Toast.makeText(CirrentApplication.getAppContext(), R.string.failed_to_return_to_network, Toast.LENGTH_LONG).show();
                        showFragment(new HomeFragment(), false);
                    }
                });
        //---------------------------
    }

    private void setStatusText(View view) {
        TextView textStatus = (TextView) view.findViewById(R.id.text_status);
        textStatus.setText(R.string.disconnect_from_soft_ap);
    }
}
