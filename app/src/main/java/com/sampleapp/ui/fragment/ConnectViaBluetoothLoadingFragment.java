package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.cirrent.cirrentsdk.net.model.DeviceInfo;
import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.cirrent.cirrentsdk.service.BluetoothService;
import com.cirrent.networkintelligencedata.onboarding.EndData;
import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.OnboardingType;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.google.gson.Gson;
import com.sampleapp.CirrentApplication;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.Utils;

import java.util.List;

public class ConnectViaBluetoothLoadingFragment extends LocalConnectionLoadingFragment implements BluetoothService.BluetoothDeviceConnectionCallback, BluetoothService.BluetoothDeviceInfoCallback {
    private TextView textStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setStatusText(view);
        connectToDeviceViaBluetooth();

        MobileAppIntelligence.setOnboardingType(OnboardingType.BLE);

        return view;
    }

    private void setStatusText(View view) {
        textStatus = (TextView) view.findViewById(R.id.text_status);
        textStatus.setText(R.string.searching_device);
    }

    private void connectToDeviceViaBluetooth() {
        String blePrefix = Prefs.CA_BLE_PREFIX.getValue();

        //----- SDK call ------------
        BluetoothService
                .getBluetoothService()
                .connectToDeviceViaBluetooth(
                        blePrefix,
                        CirrentApplication.getAppContext(),
                        this);
        //---------------------------
    }

    @Override
    public void onError(BluetoothService.BluetoothDeviceConnectionError error) {
        switch (error) {
            case BLE_NOT_SUPPORTED:
                endOnboarding("bluetooth_not_supported", R.string.ble_not_supported);
                break;
            case BLUETOOTH_DISABLED:
                endOnboarding("bluetooth_turned_off", R.string.bt_turned_off);
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
            case OPERATION_TIME_LIMIT_EXCEEDED:
            case UNABLE_TO_WRITE_DATA:
            case UNABLE_TO_READ_RESPONSE:
            case UNABLE_TO_DISCOVER_SERVICES:
            case CONNECTION_INTERRUPTED_BY_ANOTHER_SIDE:
            case UNABLE_TO_CONNECT:
                endOnboardingWithCommunicationError(error.toString());
                break;
            case FAILED_TO_FIND:
                endOnboarding(
                        "failed_to_find_device",
                        R.string.failed_to_find_device_via_bluetooth
                );
                break;
        }
    }

    @Override
    public void onDeviceConnectedSuccessfully() {
        textStatus.setText(R.string.connected_getting_info);
        getDeviceInfoViaBluetooth();
    }

    private void getDeviceInfoViaBluetooth() {
        //----- SDK call ------------
        BluetoothService
                .getBluetoothService()
                .getDeviceInfoViaBluetooth(
                        this);
        //---------------------------
    }

    @Override
    public void onError(BluetoothService.BluetoothDeviceInfoError error) {
        switch (error) {
            case CONNECTION_IS_NOT_ESTABLISHED:
                endOnboarding("connection_is_not_established", R.string.not_connected);
                break;
            case OPERATION_TIME_LIMIT_EXCEEDED:
            case UNABLE_TO_WRITE_DATA:
            case UNABLE_TO_READ_RESPONSE:
            case UNABLE_TO_DISCOVER_SERVICES:
            case CONNECTION_INTERRUPTED_BY_ANOTHER_SIDE:
                endOnboardingWithCommunicationError(error.toString());
                break;
            case INVALID_SCD_PUBLIC_KEY_RECEIVED:
                endOnboarding(
                        "invalid_scdPublicKey",
                        R.string.invalid_scd_public_key_reboot_device_and_try_again
                );
                break;
        }
    }

    @Override
    public void onInfoReceived(DeviceInfo deviceInfo, List<WiFiNetwork> candidateNetworks) {
        String deviceId = (deviceInfo.getDeviceId() == null || deviceInfo.getDeviceId().isEmpty()) ? "<unknown>" : deviceInfo.getDeviceId();
        MobileAppIntelligence.setOnboardingDeviceInfo(deviceId, getAppVersion());
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.SUCCESS,
                        "enter_creds",
                        "connected_via_ble"
                )
        );

        Utils.addHistoryItem(deviceInfo, OnboardingType.BLE);

        String serializedCandidateNetworks = new Gson().toJson(candidateNetworks);
        showFragment(
                SetupDeviceViaBluetoothFragment.newInstance(
                        deviceInfo.getDeviceId(),
                        serializedCandidateNetworks
                ),
                false
        );
    }
}
