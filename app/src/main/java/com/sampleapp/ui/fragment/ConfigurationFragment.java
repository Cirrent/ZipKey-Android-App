package com.sampleapp.ui.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.sampleapp.BuildConfig;
import com.sampleapp.MaiConstants;
import com.sampleapp.Prefs;
import com.sampleapp.R;

import java.util.HashMap;

public class ConfigurationFragment extends BaseFragment {

    private EditText editSoftApSsid;
    private EditText editBlePrefix;
    private RadioButton radioBle;
    private RadioButton radioSoftAp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_configuration, container, false);
        initViews(view);

        changeActionBarState(false, false, getString(R.string.configuration));

        return view;
    }

    private void initViews(View view) {
        editSoftApSsid = (EditText) view.findViewById(R.id.edit_soft_ap_ssid);
        editBlePrefix = (EditText) view.findViewById(R.id.edit_prefix);

        radioBle = (RadioButton) view.findViewById(R.id.radio_ble);
        radioSoftAp = (RadioButton) view.findViewById(R.id.radio_softap);
        if (Prefs.IS_SOFTAP_ONBOARDING_ACTIVATED.getValue()) {
            radioSoftAp.setChecked(true);
        } else {
            radioBle.setChecked(true);
        }
        setAppVersion(view);
        setupEditTextFields();
        setFloatButtonClickListener(view);
        setEraseDataLink(view.findViewById(R.id.text_erase_personal_data));
    }

    private void setAppVersion(View view) {
        final TextView textVersion = (TextView) view.findViewById(R.id.text_version);
        final TextView textVersionCode = (TextView) view.findViewById(R.id.text_version_code);

        final String versionCode = "Build " + BuildConfig.VERSION_CODE;
        final String versionName = BuildConfig.VERSION_NAME;
        textVersion.setText(versionName);
        textVersionCode.setText(versionCode);
    }

    private void setupEditTextFields() {
        if (Prefs.SOFT_AP_SSID.exists()) {
            final String softApSsid = Prefs.SOFT_AP_SSID.getValue();
            editSoftApSsid.setText(String.valueOf(softApSsid));
        }

        if (Prefs.CA_BLE_PREFIX.exists()) {
            final String prefix = Prefs.CA_BLE_PREFIX.getValue();
            editBlePrefix.setText(String.valueOf(prefix));
        }
    }

    private void setFloatButtonClickListener(View view) {
        view.findViewById(R.id.floating_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioBle.isChecked()) {
                    String blePrefix = String.valueOf(editBlePrefix.getText()).trim();
                    if (blePrefix.isEmpty()) {
                        showToast("Please enter BLE Service prefix", Toast.LENGTH_LONG);
                    } else {
                        Prefs.CA_BLE_PREFIX.setValue(blePrefix);
                        Prefs.IS_SOFTAP_ONBOARDING_ACTIVATED.setValue(false);
                        showHomeFragment();
                    }
                }

                if (radioSoftAp.isChecked()) {
                    String softApSsid = String.valueOf(editSoftApSsid.getText()).trim();
                    if (softApSsid.isEmpty()) {
                        showToast("Please enter Soft AP SSID", Toast.LENGTH_LONG);
                    } else {
                        Prefs.SOFT_AP_SSID.setValue(softApSsid);
                        Prefs.IS_SOFTAP_ONBOARDING_ACTIVATED.setValue(true);
                        showHomeFragment();
                    }
                }
            }
        });
    }

    public void setEraseDataLink(TextView textView) {
        final String supportEmail = "support@cirrent.com";
        final String eraseDataText = "I want to get my personal data erased";
        SpannableString ss = new SpannableString(eraseDataText);
        final ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                requireActivity().startActivity(
                        new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + supportEmail))
                );
            }
        };
        ss.setSpan(clickableSpan, 0, eraseDataText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(ss, TextView.BufferType.SPANNABLE);
    }

    private void showHomeFragment() {
        final HashMap<String, String> debug = new HashMap<String, String>() {
            {
                put("softap_ssid", Prefs.SOFT_AP_SSID.getValue());
                put("ble_prefix", Prefs.CA_BLE_PREFIX.getValue());
                put("is_softap_onboarding_activated", String.valueOf(Prefs.IS_SOFTAP_ONBOARDING_ACTIVATED.getValue()));
            }
        };
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.SUCCESS,
                        MaiConstants.WAITING_ONBOARDING,
                        "configuration_saved"
                ).setDebugInfo(debug)
        );

        showToast(R.string.configuration_is_saved, Toast.LENGTH_SHORT);
        showFragment(new HomeFragment(), false);
    }
}
