package com.sampleapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.sampleapp.CirrentApplication;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.ui.FragmentListener;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public abstract class BaseActivity extends AppCompatActivity implements FragmentListener {

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
        showStartActivity();
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