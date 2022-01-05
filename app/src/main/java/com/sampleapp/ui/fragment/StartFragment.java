package com.sampleapp.ui.fragment;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.Utils;
import com.sampleapp.ui.activity.HomeActivity;

import static com.sampleapp.MaiConstants.STARTING_LOGIN;
import static com.sampleapp.MaiConstants.WAITING_ONBOARDING;

public class StartFragment extends BaseFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_start, container, false);
        setDefaultOnboardingValues();
        setupViews(view);

        return view;
    }

    private void setDefaultOnboardingValues() {
        final Prefs.ValueWrapper<String> blePrefix = Prefs.CA_BLE_PREFIX;
        final String blePrefixValue = blePrefix.getValue();
        if (blePrefixValue == null || blePrefixValue.isEmpty()) {
            blePrefix.setValue("CA_BLE");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        changeActionBarState(true, false, "");
    }

    private void setupViews(View view) {
        setupSkipButton(view);
        view.findViewById(R.id.button_sign_in).setOnClickListener(this);
        view.findViewById(R.id.button_skip).setOnClickListener(this);
        Utils.setupTermsAndPrivacyLinks((TextView) view.findViewById(R.id.text_privacy_links), requireActivity());
    }

    private void setupSkipButton(View view) {
        TextView textSkip = (TextView) view.findViewById(R.id.button_skip);
        textSkip.setPaintFlags(textSkip.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textSkip.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                MobileAppIntelligence.enterStep(
                        StepData.create(
                                StepResult.SUCCESS,
                                STARTING_LOGIN,
                                "moved_to_login_screen"
                        )
                );
                showFragment(new LoginFragment(), true);
                break;
            case R.id.button_skip:
                MobileAppIntelligence.enterStep(
                        StepData.create(
                                StepResult.SUCCESS,
                                WAITING_ONBOARDING,
                                "login_skipped"
                        )
                );
                Intent intent = new Intent(getContext(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                requireActivity().startActivity(intent);
                break;
        }
    }
}
