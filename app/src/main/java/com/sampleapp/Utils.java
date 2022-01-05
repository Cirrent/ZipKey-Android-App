package com.sampleapp;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.cirrent.cirrentsdk.net.model.DeviceInfo;
import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.OnboardingType;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.google.gson.Gson;
import com.sampleapp.history.OnboardingHistoryPrefManager;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Utils {
    public static void setupTermsAndPrivacyLinks(TextView textView, final Activity activity) {
        final String urlPrivacyPolicy = "https://www.cirrent.com/privacy-policy";
        final String urlTermsOfService = "https://www.cirrent.com/terms-of-service/";
        final String termsOfService = "Terms of service";
        final String privacyPolicy = "Privacy Policy";
        String text = termsOfService + " " + "and" + " " + privacyPolicy;
        SpannableString ss = new SpannableString(text);
        Map<String, ClickableSpan> links = new TreeMap<>();
        links.put(termsOfService, new ClickableSpan() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlTermsOfService)));
            }
        });
        links.put(privacyPolicy, new ClickableSpan() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlPrivacyPolicy)));
            }
        });

        for (Map.Entry<String, ClickableSpan> entry : links.entrySet()) {
            int linkIndexStart = text.indexOf(entry.getKey());
            int linkIndexEnd = linkIndexStart + entry.getKey().length();
            ss.setSpan(entry.getValue(), linkIndexStart, linkIndexEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(ss, TextView.BufferType.SPANNABLE);
    }

    public static String encodeCredentialsToBase64(String login, String password) {
        final String prefix = "Basic ";
        final String concatenatedCredentials = login + ":" + password;
        final String encodedCredentials = prefix + Base64.encodeToString(concatenatedCredentials.getBytes(), Base64.NO_WRAP);
        return encodedCredentials;
    }

    public static void addHistoryItem(DeviceInfo deviceInfo, OnboardingType onboardingType) {
        new OnboardingHistoryPrefManager(new Gson()).addHistoryItem(deviceInfo, onboardingType);
    }

    public static String getFormattedTimeStr(Long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return dateFormat.format(calendar.getTime());
    }

    public static String getFormattedOnboardingType(OnboardingType onboardingType) {
        final String formattedType;
        if (onboardingType == OnboardingType.SOFTAP) {
            formattedType = CirrentApplication.getAppContext().getString(R.string.soft_ap_onboarding_type);
        } else {
            formattedType = CirrentApplication.getAppContext().getString(R.string.ble_onboarding_type);
        }
        return formattedType;
    }

    public static String getSsid(Context context) {
        String ssid = "";
        if (context == null) {
            return ssid;
        }

        WifiInfo wifiInfo = getWifiManager(context).getConnectionInfo();
        if (wifiInfo != null) {
            String dirtySsid = wifiInfo.getSSID();
            ssid = dirtySsid.replace("\"", "");
        }
        return ssid.trim();
    }

    public static void sendDisconnectingStep(final String currentSsid) {
        final HashMap<String, String> debug = new HashMap<String, String>() {
            {
                put("current_ssid", currentSsid);
            }
        };
        MobileAppIntelligence.enterStep(
                StepData.create(
                        StepResult.SUCCESS,
                        "disconnecting_from_soft_ap_ssid",
                        "softap_ssid_connection_detected"
                ).setDebugInfo(debug)
        );
    }

    public static int getWifiNetworkId(Context context) {
        WifiInfo wifiInfo = getWifiManager(context).getConnectionInfo();

        return wifiInfo.getNetworkId();
    }

    public static boolean isSoftApNetworkBound(Context context) {
        Network bindNetwork = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            bindNetwork = connectivityManager.getBoundNetworkForProcess();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bindNetwork = ConnectivityManager.getProcessDefaultNetwork();
        }
        return bindNetwork != null;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isDeviceConnectedToAp(Context context) {
        final WifiManager wifiManager = getWifiManager(context);
        if (wifiManager.isWifiEnabled()) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                return !cm.isActiveNetworkMetered();
            }
        }

        return false;
    }

    private static WifiManager getWifiManager(Context context) {
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public static ObjectAnimator startConstantVerticalViewAnimation(View view) {
        ObjectAnimator flipAnimator = ObjectAnimator.ofFloat(view, "rotationY", 0f, 360f);
        flipAnimator.setDuration(1000);
        flipAnimator.setRepeatMode(ObjectAnimator.RESTART);
        flipAnimator.setRepeatCount(ValueAnimator.INFINITE);
        flipAnimator.start();
        return flipAnimator;
    }

    public static void requestLocationPermission(final Activity activity, final RequestLocationCallback requestLocationCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String permission = Manifest.permission.ACCESS_FINE_LOCATION;
            final String dialogMessage = activity.getString(R.string.location_service_permission_needed);
            if (activity.shouldShowRequestPermissionRationale(permission)) {
                final String buttonName = activity.getString(R.string.ok);
                showDialog(activity, dialogMessage, buttonName, requestLocationCallback, new DialogAction() {
                    @Override
                    public void doAction() {
                        final int requestCode = 114;
                        ActivityCompat
                                .requestPermissions(
                                        activity,
                                        new String[]{permission},
                                        requestCode
                                );
                    }
                });
            } else {
                final String buttonName = activity.getString(R.string.go_to_settings);
                showDialog(activity, dialogMessage, buttonName, requestLocationCallback, new DialogAction() {
                    @Override
                    public void doAction() {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        activity.startActivity(intent);
                    }
                });
            }
        }
    }

    public static void requestLocationService(final Activity activity, final RequestLocationCallback requestLocationCallback) {
        final String dialogMessage = activity.getString(R.string.location_service_should_be_enabled);
        final String buttonName = activity.getString(R.string.go_to_settings);
        Utils.showDialog(activity, dialogMessage, buttonName, requestLocationCallback, new DialogAction() {
            @Override
            public void doAction() {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }
        });
    }

    private static void showDialog(final Context context,
                                   final String message,
                                   final String buttonName,
                                   final RequestLocationCallback requestLocationCallback,
                                   final DialogAction action) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder
                .setMessage(message)
                .setPositiveButton(buttonName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        action.doAction();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        requestLocationCallback.onDialogCanceled();
                    }
                })
                .create()
                .show();
    }

    public interface RequestLocationCallback {
        void onDialogCanceled();
    }

    private interface DialogAction {
        void doAction();
    }
}
