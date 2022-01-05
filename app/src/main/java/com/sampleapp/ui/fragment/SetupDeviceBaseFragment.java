package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sampleapp.R;

import java.util.Arrays;
import java.util.List;

public abstract class SetupDeviceBaseFragment extends BaseFragment {
    private String selectedSecurityType;
    private Spinner spinnerSsidList;
    private EditText editTextPassword;
    private FloatingActionButton layoutFloatingButton;
    private CheckBox checkBoxHiddenNetwork;
    private String hiddenSsid;
    String deviceId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_setup_network, container, false);

        changeActionBarState(false, true, "");
        initViews(view);

        return view;
    }

    private void initViews(View view) {
        final RelativeLayout layoutHiddenNetwork = (RelativeLayout) view.findViewById(R.id.layout_hidden_ssid);
        final RelativeLayout layoutWifiNetworks = (RelativeLayout) view.findViewById(R.id.layout_wifi_networks);
        final TextView textHintWifiPassword = (TextView) view.findViewById(R.id.text_hint_wifi_password);
        final TextView textHeader = (TextView) view.findViewById(R.id.text_header);
        final EditText editHiddenSsid = (EditText) view.findViewById(R.id.edit_hidden_ssid);
        final Spinner spinnerSecurityTypes = (Spinner) view.findViewById(R.id.list_security_types);
        final CheckBox checkBoxShowPassword = (CheckBox) view.findViewById(R.id.checkbox_show_password);
        checkBoxHiddenNetwork = (CheckBox) view.findViewById(R.id.checkbox_add_hidden_network);
        spinnerSsidList = (Spinner) view.findViewById(R.id.list_wifi_networks);
        editTextPassword = (EditText) view.findViewById(R.id.edit_pre_shared_password);
        layoutFloatingButton = (FloatingActionButton) view.findViewById(R.id.floating_action_button);
        String notEmptyDeviceId = (deviceId == null || deviceId.isEmpty()) ? "your device" : deviceId;
        textHeader.setText(String.format("Connect %s to your home Wi-Fi network", notEmptyDeviceId));

        setFloatingButtonOnClickListener(checkBoxHiddenNetwork.isChecked());

        setupHiddenNetworkCheckBoxListener(
                layoutHiddenNetwork,
                layoutWifiNetworks,
                textHintWifiPassword,
                editHiddenSsid,
                spinnerSecurityTypes
        );
        setupShowPassCheckboxListener(checkBoxShowPassword);
    }

    private void setupHiddenNetworkCheckBoxListener(final RelativeLayout layoutHiddenNetwork, final RelativeLayout layoutWifiNetworks, final TextView textHintWifiPassword, final EditText editHiddenSsid, final Spinner spinnerSecurityTypes) {
        checkBoxHiddenNetwork.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleViews(isChecked);
            }

            private void toggleViews(boolean isChecked) {
                setFloatingButtonOnClickListener(isChecked);

                if (isChecked) {
                    toggleFloatingButtonVisibility(editHiddenSsid.getText());
                    setupSsidFieldTextChangedListener();
                    setupSecurityTypesSpinner();
                    editHiddenSsid.requestFocus();

                    layoutHiddenNetwork.setVisibility(View.VISIBLE);
                    layoutWifiNetworks.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams editPassParams = (RelativeLayout.LayoutParams) textHintWifiPassword.getLayoutParams();
                    editPassParams.addRule(RelativeLayout.BELOW, R.id.layout_hidden_ssid);
                } else {
                    toggleFloatingButtonVisibility((CharSequence) spinnerSsidList.getSelectedItem());
                    layoutHiddenNetwork.setVisibility(View.GONE);
                    layoutWifiNetworks.setVisibility(View.VISIBLE);

                    RelativeLayout.LayoutParams editPassParams = (RelativeLayout.LayoutParams) textHintWifiPassword.getLayoutParams();
                    editPassParams.addRule(RelativeLayout.BELOW, R.id.layout_wifi_networks);
                }
            }

            private void toggleFloatingButtonVisibility(CharSequence text) {
                if (text != null && text.length() > 0) {
                    layoutFloatingButton.setVisibility(View.VISIBLE);
                } else {
                    layoutFloatingButton.setVisibility(View.GONE);
                }
            }

            private void setupSsidFieldTextChangedListener() {
                editHiddenSsid.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        //nothing
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        toggleFloatingButtonVisibility(s);
                        hiddenSsid = String.valueOf(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //nothing
                    }
                });
            }

            private void setupSecurityTypesSpinner() {
                final List<String> securityTypes = Arrays.asList(getResources().getStringArray(R.array.security_types));
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getContext(), R.layout.item_spinner, securityTypes);
                spinnerSecurityTypes.setAdapter(spinnerAdapter);
                spinnerSecurityTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedSecurityType = securityTypes.get(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        //nothing
                    }
                });
            }
        });
    }

    private void setupShowPassCheckboxListener(CheckBox checkBoxShowPassword) {
        checkBoxShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    editTextPassword.setSelection(editTextPassword.getText().length());
                } else {
                    editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    editTextPassword.setSelection(editTextPassword.getText().length());
                }
            }
        });
    }

    protected boolean isOpenSecurityTypeSelected() {
        final String securityType = "OPEN";
        return selectedSecurityType != null && selectedSecurityType.equals(securityType);
    }

    public String getHiddenSsid() {
        return hiddenSsid;
    }

    public Spinner getSsidListSpinner() {
        return spinnerSsidList;
    }

    public String getSelectedSecurityType() {
        return selectedSecurityType;
    }

    public String getPreSharedKey() {
        return String.valueOf(editTextPassword.getText());
    }

    public FloatingActionButton getLayoutFloatingButton() {
        return layoutFloatingButton;
    }

    public abstract void setFloatingButtonOnClickListener(boolean isHiddenNetworkSetup);
}
