package com.sampleapp.history;

import com.cirrent.cirrentsdk.net.model.DeviceInfo;
import com.cirrent.networkintelligencedata.onboarding.OnboardingType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.Prefs;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OnboardingHistoryPrefManager {
    private final Gson gson;

    public OnboardingHistoryPrefManager(Gson gson) {
        this.gson = gson;
    }

    public List<OnboardingInfo> getHistory() {
        String serializedJson = Prefs.ONBOARDING_HISTORY.getValue();
        return convertOnBoardingInfoJSONToModels(serializedJson);
    }

    public void addHistoryItem(DeviceInfo deviceInfo, OnboardingType onboardingType) {
        String deviceId = deviceInfo.getDeviceId();
        String unknownDeviceId = "<unknown>";
        String notEmptyDeviceId = (deviceId == null || deviceId.isEmpty()) ? unknownDeviceId : deviceInfo.getDeviceId();
        String accountId = deviceInfo.getAccountId();
        String notEmptyAccountId = (accountId == null || accountId.isEmpty()) ? unknownDeviceId : deviceInfo.getAccountId();
        Long time = System.currentTimeMillis();
        OnboardingInfo onboardingInfo = new OnboardingInfo(notEmptyDeviceId, notEmptyAccountId, time, onboardingType);
        addHistoryItem(onboardingInfo);
    }

    public void addHistoryItem(OnboardingInfo onboardingInfo) {
        String serializedJson = Prefs.ONBOARDING_HISTORY.getValue();
        List<OnboardingInfo> onboardingInfoModels = convertOnBoardingInfoJSONToModels(serializedJson);
        onboardingInfoModels.add(onboardingInfo);
        serializedJson = convertOnBoardingInfoModelsToJSON(onboardingInfoModels);
        Prefs.ONBOARDING_HISTORY.setValue(serializedJson);
    }

    public void clearHistory() {
        Prefs.ONBOARDING_HISTORY.remove();
    }

    private String convertOnBoardingInfoModelsToJSON(List<OnboardingInfo> onboardingInfoList) {
        return gson.toJson(onboardingInfoList);
    }

    private List<OnboardingInfo> convertOnBoardingInfoJSONToModels(String serializedJSON) {
        Type type = new TypeToken<List<OnboardingInfo>>() {
        }.getType();
        List<OnboardingInfo> onboardingInfoList = gson.fromJson(serializedJSON, type);
        return onboardingInfoList == null ? new ArrayList<OnboardingInfo>() : onboardingInfoList;
    }
}
