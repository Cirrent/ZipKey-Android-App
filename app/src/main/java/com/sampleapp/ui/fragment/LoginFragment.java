package com.sampleapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.sampleapp.MaiConstants;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;
import com.sampleapp.Utils;
import com.sampleapp.net.requester.LoginRequester;
import com.sampleapp.ui.activity.HomeActivity;

import java.util.HashMap;

public class LoginFragment extends BaseFragment implements View.OnClickListener {

    private EditText fieldLogin;
    private EditText fieldPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {
        fieldLogin = (EditText) view.findViewById(R.id.edit_login);
        fieldPassword = (EditText) view.findViewById(R.id.edit_password);
        CheckBox checkBoxShowPassword = (CheckBox) view.findViewById(R.id.checkbox_show_password);
        view.findViewById(R.id.img_back_arrow).setOnClickListener(this);
        view.findViewById(R.id.button_sign_in).setOnClickListener(this);

        setupShowPassCheckboxListener(checkBoxShowPassword);
    }

    private void setupShowPassCheckboxListener(CheckBox checkBoxShowPassword) {
        checkBoxShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fieldPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    fieldPassword.setSelection(fieldPassword.getText().length());
                } else {
                    fieldPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    fieldPassword.setSelection(fieldPassword.getText().length());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back_arrow:
                MobileAppIntelligence.enterStep(
                        StepData.create(
                                StepResult.SUCCESS,
                                MaiConstants.WAITING_FIRST_ACTION,
                                "back_from_login_screen"
                        )
                );
                requireActivity().onBackPressed();
                break;
            case R.id.button_sign_in:
                final String username = String.valueOf(fieldLogin.getText());
                final String password = String.valueOf(fieldPassword.getText());
                login(username, password);
                break;
        }
    }

    private void login(final String username, final String password) {
        final SimpleProgressDialog progressDialog = new SimpleProgressDialog(getActivity(), R.string.logging_in);
        new LoginRequester(getContext(), username, password) {
            @Override
            public void onSuccess(String accountId) {
                MobileAppIntelligence.enterStep(
                        StepData.create(
                                StepResult.SUCCESS,
                                MaiConstants.WAITING_ONBOARDING,
                                "logged_in"
                        )
                );
                saveEncodedCredentials();
                Prefs.ACCOUNT_ID.setValue(accountId);
                Prefs.APP_ID.setValue(String.valueOf(fieldLogin.getText()));
                Prefs.ONBOARDING_HISTORY.remove();
                startHomeActivity();
            }

            @Override
            public void onFailure(String error, int statusCode, final String errorBody) {
                super.onFailure(error, statusCode, errorBody);
                MobileAppIntelligence.enterStep(
                        StepData.create(
                                StepResult.FAILURE,
                                MaiConstants.STARTING_LOGIN,
                                String.valueOf(statusCode)
                        ).setDebugInfo(
                                new HashMap<String, String>() {
                                    {
                                        put("error", errorBody);
                                    }
                                }
                        )
                );
            }
        }.doRequest(progressDialog);
    }

    private void saveEncodedCredentials() {
        final String username = String.valueOf(fieldLogin.getText());
        final String password = String.valueOf(fieldPassword.getText());
        final String encodedCredentials = Utils.encodeCredentialsToBase64(username, password);
        Prefs.ENCODED_CREDENTIALS.setValue(encodedCredentials);
    }

    private void startHomeActivity() {
        Utils.hideKeyboard(requireActivity());
        Intent intent = new Intent(getContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        requireActivity().startActivity(intent);
    }
}
