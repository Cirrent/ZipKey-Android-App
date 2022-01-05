package com.sampleapp.history;

import com.cirrent.networkintelligencedata.onboarding.OnboardingType;
import com.google.gson.annotations.SerializedName;

public class OnboardingInfo {
    @SerializedName("device_id")
    private final String deviceId;
    @SerializedName("account_id")
    private final String accountId;
    @SerializedName("onboarding_time")
    private final Long onboardingTime;
    @SerializedName("onboarding_type")
    private final OnboardingType onboardingType;

    public OnboardingInfo(String deviceId, String accountId, Long onboardingTime, OnboardingType onboardingType) {
        this.deviceId = deviceId;
        this.accountId = accountId;
        this.onboardingTime = onboardingTime;
        this.onboardingType = onboardingType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getAccountId() {
        return accountId;
    }

    public Long getOnboardingTime() {
        return onboardingTime;
    }

    public OnboardingType getOnboardingType() {
        return onboardingType;
    }

}
