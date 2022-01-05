package com.sampleapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.sampleapp.MaiHelper;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.ui.fragment.StartFragment;

import static com.sampleapp.MaiConstants.WAITING_FIRST_ACTION;
import static com.sampleapp.MaiConstants.WAITING_ONBOARDING;

public class StartActivity extends BaseActivity {

    private ActionBar supportActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setupActionBar();
        new MaiHelper().initializeMobileAppIntelligence();
        showAppropriateScreen();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
            supportActionBar.hide();
        }
    }

    private void showAppropriateScreen() {
        if (Prefs.ACCOUNT_ID.exists()) {
            MobileAppIntelligence.enterStep(StepData.create(WAITING_ONBOARDING));
            Intent startHomeActivity = new Intent(this, HomeActivity.class);
            startHomeActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startHomeActivity);
        } else {
            MobileAppIntelligence.enterStep(StepData.create(WAITING_FIRST_ACTION));
            showFragment(new StartFragment(), false);
        }
    }

    @Override
    public void changeActionBarState(boolean hideActionBar, boolean enableBackArrow, String title) {
        if (hideActionBar) {
            supportActionBar.hide();
            hideStatusBar();
            return;
        } else {
            supportActionBar.show();
            showStatusBar();
        }

        if (title.isEmpty()) {
            supportActionBar.setDisplayShowTitleEnabled(false);
        } else {
            supportActionBar.setDisplayShowTitleEnabled(true);
            supportActionBar.setTitle(title);
        }

        supportActionBar.setDisplayHomeAsUpEnabled(enableBackArrow);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void hideStatusBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
    }

    private void showStatusBar() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}
