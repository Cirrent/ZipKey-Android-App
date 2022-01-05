package com.sampleapp;

import android.util.Log;

import com.cirrent.networkintelligencedata.onboarding.EndData;
import com.cirrent.networkintelligencedata.onboarding.MAIError;
import com.cirrent.networkintelligencedata.onboarding.MaiCallback;
import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.OnboardingType;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.sampleapp.net.requester.MAITokenRequester;

import java.util.HashMap;
import java.util.UUID;

public class MaiHelper extends MaiCallback {

    public static final String TAG = "MAI";

    public void initializeMobileAppIntelligence() {
        final String encodedCredentialsValue = Prefs.ENCODED_CREDENTIALS.getValue();
        if (encodedCredentialsValue != null && !encodedCredentialsValue.isEmpty()) {
            Log.i(TAG, "The user is logged in. Getting token from the cloud. App ID: " + Prefs.APP_ID.getValue());
            initMaiUsingCloudToken(this);
        } else {
            initMaiUsingGeneratedToken();
        }
    }

    @Override
    public void onFailed(MAIError e) {
        final MAIError.Type errorType = e.getType();
        Log.e(
                TAG,
                String.format(
                        "%s, errorCode: %s, message: %s",
                        errorType.toString(),
                        e.getErrorCode(),
                        e.getMessage()
                )
        );
        switch (errorType) {
            case REQUEST_FAILED:
            case INIT_REQUIRED:
            case INIT_DATA_COLLECTING_IS_ACTIVE:
            case LACK_OF_LOCATION_PERMISSION:
            case RESERVED_STEP_NAME_USED:
            case START_ONBOARDING_REQUIRED:
            case ONBOARDING_TYPE_REQUIRED:
                break;
            case END_ONBOARDING_REQUIRED:
                MobileAppIntelligence.endOnboarding(EndData.createFailure("new_onboarding_started"));
                MobileAppIntelligence.startOnboarding();
                break;
        }
    }

    @Override
    public void onTokenInvalid(final MobileAppIntelligence.Retrier retrier) {
        Log.e(TAG, "onTokenInvalid received");

        final String encodedCredentialsValue = Prefs.ENCODED_CREDENTIALS.getValue();
        if (encodedCredentialsValue != null && !encodedCredentialsValue.isEmpty()) {
            Log.i(TAG, "The user is logged in. Getting token from the cloud. App ID: " + Prefs.APP_ID.getValue());
            retryWithNewTokenFromCloud(retrier, encodedCredentialsValue);
        } else {
            String token = generateToken();
            if (token == null || token.isEmpty()) {
                Log.e(TAG, "Unable to initialize. Token is null or empty.");
            } else {
                retrier.retry(token);
            }
        }
    }

    private void retryWithNewTokenFromCloud(final MobileAppIntelligence.Retrier retrier, final String encodedCredentialsValue) {
        new MAITokenRequester(CirrentApplication.getAppContext(), encodedCredentialsValue) {
            @Override
            public void onSuccess(String analyticsToken) {
                retrier.retry(analyticsToken);
            }

            @Override
            public void onFailure(String error, int statusCode, String errorBody) {
                super.onFailure(error, statusCode, errorBody);
            }
        }.doRequest(null);
    }

    private void initMaiUsingCloudToken(final MaiHelper maiHelper) {
        MobileAppIntelligence.init(CirrentApplication.getAppContext(), Prefs.TOKEN.getValue(), maiHelper);
    }

    private void initMaiUsingGeneratedToken() {
        String token = generateToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Unable to initialize. Token is null or empty.");
        } else {
            MobileAppIntelligence.init(CirrentApplication.getAppContext(), token, this);
        }
    }

    private String generateToken() {
        final String accountId = "accountID";
        final String apiKey = "apiKey";
        final String apiSecret = "apiSecret";
        final String appId = getAppId();

        Log.i(TAG, "The user wasn't logged in. Default creds were used for token generation. App ID: " + appId);

        return MobileAppIntelligence
                .createToken(
                        900,
                        accountId,
                        appId,
                        apiKey,
                        apiSecret
                );
    }

    private String getAppId() {
        final String savedAppIdValue = Prefs.APP_ID.getValue();
        if (savedAppIdValue != null && !savedAppIdValue.isEmpty()) {
            return savedAppIdValue;
        } else {
            final String uuid = UUID.randomUUID().toString();
            Prefs.APP_ID.setValue(uuid);
            return uuid;
        }
    }
}
