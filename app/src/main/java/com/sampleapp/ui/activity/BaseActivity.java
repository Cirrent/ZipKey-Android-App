package com.sampleapp.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.sampleapp.CirrentApplication;
import com.sampleapp.MaiHelper;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.Utils;
import com.sampleapp.ui.FragmentListener;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public abstract class BaseActivity extends AppCompatActivity implements FragmentListener {
    public static final int LOCATION_REQUEST_CODE = 980;
    public static final int maxNumberOfLocationRequests = 2;
    public static int numOfLocationPermissionRequested = 0;

    @Override
    public void showFragment(Fragment fragment, boolean addToBackStack) {
        String fragmentName = fragment.getClass().getName();
        FragmentManager fManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fManager.beginTransaction();
        fragmentTransaction
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.content_frame, fragment, fragmentName);

        if (addToBackStack && fManager.findFragmentByTag(fragmentName) == null) {
            fragmentTransaction.addToBackStack(fragmentName);
        }

        fragmentTransaction.commit();
    }

    @Override
    public abstract void changeActionBarState(boolean hideActionBar, boolean enableBackArrow, String title);

    @Override
    public void showToast(int resId, int duration) {
        Toast.makeText(CirrentApplication.getAppContext(), resId, duration).show();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void logOut() {
        sanitizePreferences();
        MobileAppIntelligence.removeAllCollectedData(CirrentApplication.getAppContext());
        MobileAppIntelligence.cancel();
        Prefs.ONBOARDING_HISTORY.remove();
        showStartActivity();
    }

    @Override
    public int requestFineLocationPermission(boolean showOwnDialogIfNumOfRequestsExceedsLimit) {
        int permissionStatus = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        );
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            new MaiHelper().initializeMobileAppIntelligence();
            return PackageManager.PERMISSION_GRANTED;
        }

        if (numOfLocationPermissionRequested < maxNumberOfLocationRequests) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE
            );
            numOfLocationPermissionRequested++;
        } else if (showOwnDialogIfNumOfRequestsExceedsLimit) {
            Utils.requestLocationPermission(this, new Utils.RequestLocationCallback() {
                @Override
                public void onDialogCanceled() {
                    // ignore
                }
            });
        }
        return PackageManager.PERMISSION_DENIED;
    }

    private void showStartActivity() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void sanitizePreferences() {
        Prefs.APP_ID.remove();
        Prefs.SOFT_AP_SSID.remove();
        Prefs.WIFI_NETWORK_ID.remove();
        Prefs.PRIVATE_SSID.remove();
        Prefs.ENCODED_CREDENTIALS.remove();
        Prefs.ACCOUNT_ID.remove();
        Prefs.CA_BLE_PREFIX.setValue("CA_BLE");
        Prefs.SOFT_AP_SSID.remove();
        Prefs.IS_SOFTAP_ONBOARDING_ACTIVATED.remove();
    }
}