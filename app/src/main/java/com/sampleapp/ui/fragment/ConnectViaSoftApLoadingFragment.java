package com.sampleapp.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.net.model.DeviceInfo;
import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.cirrent.cirrentsdk.service.SoftApService;
import com.cirrent.networkintelligencedata.onboarding.EndData;
import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.OnboardingType;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.google.gson.Gson;
import com.sampleapp.CirrentApplication;
import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;
import com.sampleapp.Utils;

import java.util.HashMap;
import java.util.List;

public class ConnectViaSoftApLoadingFragment extends LocalConnectionLoadingFragment implements SoftApService.SoftApDeviceConnectionCallback, SoftApService.SoftApDeviceInfoCallback {
    private static final String SOFT_AP_SSID = "softApSsid";
    private String softApSsid;

    public static ConnectViaSoftApLoadingFragment newInstance(String softApSsid) {
        ConnectViaSoftApLoadingFragment fragment = new ConnectViaSoftApLoadingFragment();
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

        MobileAppIntelligence.setOnboardingType(OnboardingType.SOFTAP);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        setStatusText(view);
        connectToDeviceViaSoftAp(false);

        return view;
    }

    private void setStatusText(View view) {
        TextView textStatus = (TextView) view.findViewById(R.id.text_status);
        textStatus.setText(R.string.connecting_to_device_via_soft_ap);
    }

    private void connectToDeviceViaSoftAp(boolean suppressSmartSwitchWarning) {
        SoftApService
                .getSoftApService()
                .setSoftApConnectTimings(10, 4)
                .connectToDeviceViaSoftAp(
                        suppressSmartSwitchWarning,
                        CirrentApplication.getAppContext(),
                        softApSsid,
                        this);
    }

    @Override
    public void onDeviceConnectedSuccessfully() {
        getDeviceInfoViaSoftAp();
    }

    private void getDeviceInfoViaSoftAp() {
        //----- SDK call ------------
        SoftApService
                .getSoftApService()
                .setProgressView(new SimpleProgressDialog(getActivity(), R.string.getting_device_info_via_soft_ap))
                .setSoftApPort(80)
                .getDeviceInfoViaSoftAp(
                        CirrentApplication.getAppContext(),
                        this,
                        new CommonErrorCallback() {
                            @Override
                            public void onFailure(final CirrentException e) {
                                MobileAppIntelligence.endOnboarding(
                                        EndData.createFailure(
                                                "cant_get_info_via_softap"
                                        ).setDebugInfo(new HashMap<String, String>() {
                                            {
                                                put("error", e.getMessage());
                                            }
                                        })
                                );
                                showToast(
                                        getString(R.string.cant_get_info) + " Reason: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                );
                                showFragment(new HomeFragment(), false);
                            }
                        });
        //---------------------------
    }

    @Override
    public void onDeviceInfoReceived(DeviceInfo deviceInfo, List<WiFiNetwork> candidateNetworks) {
        String deviceId = (deviceInfo.getDeviceId() == null || deviceInfo.getDeviceId().isEmpty()) ? "<unknown>" : deviceInfo.getDeviceId();
        MobileAppIntelligence.setOnboardingDeviceInfo(deviceId, getAppVersion());
        String serializedCandidateNetworks = new Gson().toJson(candidateNetworks);
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.SUCCESS,
                        "enter_creds",
                        "connected_via_softap"
                )
        );

        Utils.addHistoryItem(deviceInfo, OnboardingType.SOFTAP);

        showFragment(
                SetupDeviceViaSoftApFragment.newInstance(
                        deviceInfo.getDeviceId(),
                        serializedCandidateNetworks
                ),
                false
        );
    }

    @Override
    public void onError(SoftApService.SoftApDeviceInfoError error) {
        if (error == SoftApService.SoftApDeviceInfoError.INVALID_SCD_PUBLIC_KEY_RECEIVED) {
            endOnboarding(
                    "invalid_scdPublicKey",
                    R.string.invalid_scd_public_key_reboot_device_and_try_again
            );
        }
    }

    @Override
    public void onError(SoftApService.SoftApDeviceConnectionError error) {
        switch (error) {
            case SOFT_AP_NETWORK_NOT_FOUND:
                endOnboarding("cant_find_softap_network", R.string.cant_find_soft_ap_network);
                break;
            case SMART_NETWORK_ENABLED:
                showSmartSwitchWarning();
                break;
            case LOCATION_PERMISSION_DENIED:
                MobileAppIntelligence.endOnboarding(EndData.createFailure("location_permission_denied"));
                Utils.requestLocationPermission(requireActivity(), new Utils.RequestLocationCallback() {
                    @Override
                    public void onDialogCanceled() {
                        showFragment(new HomeFragment(), false);
                    }
                });
                break;
            case LOCATION_DISABLED:
                MobileAppIntelligence.endOnboarding(EndData.createFailure("location_service_disabled"));
                Utils.requestLocationService(requireActivity(), new Utils.RequestLocationCallback() {
                    @Override
                    public void onDialogCanceled() {
                        showFragment(new HomeFragment(), false);
                    }
                });
                break;
            case FAILED_TO_CONNECT:
                endOnboarding(
                        "cant_connect_to_softap_network",
                        R.string.cant_connect_to_device_soft_ap_network
                );
                break;
        }
    }

    private void showSmartSwitchWarning() {
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.SUCCESS,
                        "showing_smart_switch_warning",
                        "smart_switch_enabled"
                )
        );
        final String message = getString(R.string.smart_switch_msg);
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.smart_network_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.smart_network_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MobileAppIntelligence.endOnboarding(EndData.createFailure("user_going_to_smart_switch_settings"));
                        dialog.dismiss();
                        showAdvancedWifiIfAvailable();
                    }
                })
                .setNegativeButton(getString(R.string.smart_network_negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        connectToDeviceViaSoftAp(true);
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void showAdvancedWifiIfAvailable() {
        final Intent intent = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
        final Context context = requireContext();
        if (activityExists(context, intent)) {
            context.startActivity(intent);
        }
    }

    public boolean activityExists(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        final ResolveInfo info = mgr.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (info != null);
    }
}
