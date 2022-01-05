package com.sampleapp.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cirrent.cirrentsdk.service.BluetoothService;
import com.cirrent.cirrentsdk.service.SoftApService;
import com.cirrent.networkintelligencedata.onboarding.EndData;
import com.cirrent.networkintelligencedata.onboarding.MobileAppIntelligence;
import com.cirrent.networkintelligencedata.onboarding.StepData;
import com.cirrent.networkintelligencedata.onboarding.StepResult;
import com.google.android.material.navigation.NavigationView;
import com.sampleapp.MaiHelper;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.Utils;
import com.sampleapp.ui.fragment.ConfigurationFragment;
import com.sampleapp.ui.fragment.ConnectViaBluetoothLoadingFragment;
import com.sampleapp.ui.fragment.ConnectViaSoftApLoadingFragment;
import com.sampleapp.ui.fragment.DisconnectFromSoftApLoadingFragment;
import com.sampleapp.ui.fragment.HomeFragment;
import com.sampleapp.ui.fragment.OnBoardingHistoryFragment;
import com.sampleapp.ui.fragment.SendCredentialsViaBluetoothFragment;
import com.sampleapp.ui.fragment.SendCredentialsViaSoftApFragment;
import com.sampleapp.ui.fragment.SetupDeviceViaBluetoothFragment;
import com.sampleapp.ui.fragment.SetupDeviceViaSoftApFragment;

import java.util.Arrays;
import java.util.List;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean backArrowListenerIsRegistered = false;
    private ActionBarDrawerToggle toggle;
    private ActionBar supportActionBar;
    private DrawerLayout drawer;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = setupActionBar();
        setupNavigationDrawer(toolbar);
    }

    private Toolbar setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_custom_title);
        supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
    }

    private void setupNavigationDrawer(Toolbar toolbar) {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Utils.setupTermsAndPrivacyLinks(
                (TextView) navigationView.findViewById(R.id.text_privacy_links),
                this
        );
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestFineLocationPermission(false);
        final String softApSsid = Prefs.SOFT_AP_SSID.getValue();
        final String currentSsid = Utils.getSsid(this);
        final boolean isConnectedToSoftAp = currentSsid.equals(softApSsid);
        if (!isConnectedToSoftAp
                && Utils.isSoftApNetworkBound(this)) {
            showFragment(DisconnectFromSoftApLoadingFragment.newInstance(softApSsid), false);
        } else {
            final FragmentManager supportFragmentManager = getSupportFragmentManager();
            if (!isConnectViaSoftApLoadingFragmentVisible(supportFragmentManager)) {
                Fragment homeFragment = supportFragmentManager.findFragmentByTag(HomeFragment.class.getName());
                if (homeFragment == null || !homeFragment.isVisible()) {
                    showFragment(new HomeFragment(), false);
                }
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //----- SDK call ------------
        if (!isConnectViaSoftApLoadingFragmentVisible(getSupportFragmentManager())) {
            SoftApService
                    .getSoftApService()
                    .cancelAllTasks();
        }
        //---------------------------

        //----- SDK call ------------
        BluetoothService
                .getBluetoothService()
                .cancelAllTasks();
        //---------------------------
    }

    @Override
    protected void onStop() {
        super.onStop();
        numOfLocationPermissionRequested = 0;
    }

    private boolean isConnectViaSoftApLoadingFragmentVisible(FragmentManager supportFragmentManager) {
        final Fragment connectToSoftApFragment = supportFragmentManager.findFragmentByTag(ConnectViaSoftApLoadingFragment.class.getName());
        return connectToSoftApFragment != null && connectToSoftApFragment.isVisible();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new MaiHelper().initializeMobileAppIntelligence();
        }
    }

    @Override
    public void changeActionBarState(boolean hideActionBar, boolean enableBackArrow, String title) {
        if (hideActionBar) {
            supportActionBar.hide();
            return;
        } else {
            supportActionBar.show();
        }

        if (enableBackArrow) {
            showBackArrowIcon();

            if (!backArrowListenerIsRegistered) {
                setBackArrowOnClickListener();
                backArrowListenerIsRegistered = true;
            }
        } else {
            showHamburgerIcon();
        }

        setActionBarTitle(title);
    }

    private void setActionBarTitle(String title) {
        if (title.isEmpty()) {
            toolbarTitle.setText("");
        } else {
            toolbarTitle.setText(title);
        }
    }

    private void showBackArrowIcon() {
        toggle.setDrawerIndicatorEnabled(false);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setBackArrowOnClickListener() {
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSoftApSetupFragmentShowed(getSupportFragmentManager()) &&
                        !isBleSetupFragmentShowed(getSupportFragmentManager())) {
                    showFragment(new HomeFragment(), false);
                }
            }
        });
    }

    private void showHamburgerIcon() {
        supportActionBar.setDisplayHomeAsUpEnabled(false);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.setToolbarNavigationClickListener(null);
        backArrowListenerIsRegistered = false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            final FragmentManager supportFragmentManager = getSupportFragmentManager();
            Fragment homeFragment = supportFragmentManager.findFragmentByTag(HomeFragment.class.getName());
            if (homeFragment != null) {
                if (homeFragment.isVisible()) {
                    finish();
                } else {
                    handleOnBackPressed(supportFragmentManager);
                }
            } else {
                handleOnBackPressed(supportFragmentManager);
            }
        }
    }

    private void handleOnBackPressed(FragmentManager supportFragmentManager) {
        if (isSoftApSetupFragmentShowed(supportFragmentManager) ||
                isBleSetupFragmentShowed(supportFragmentManager)) return;

        List<Fragment> fragmentsWithBlockedBackNavigation = Arrays.asList(
                supportFragmentManager.findFragmentByTag(SendCredentialsViaSoftApFragment.class.getName()),
                supportFragmentManager.findFragmentByTag(ConnectViaSoftApLoadingFragment.class.getName()),
                supportFragmentManager.findFragmentByTag(DisconnectFromSoftApLoadingFragment.class.getName()),
                supportFragmentManager.findFragmentByTag(ConnectViaBluetoothLoadingFragment.class.getName()),
                supportFragmentManager.findFragmentByTag(SendCredentialsViaBluetoothFragment.class.getName())
        );

        for (Fragment fragment : fragmentsWithBlockedBackNavigation) {
            if (fragment != null && fragment.isVisible()) {
                Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        showFragment(new HomeFragment(), false);
    }

    private boolean isSoftApSetupFragmentShowed(FragmentManager supportFragmentManager) {
        final Fragment setupViaSoftApFragment = supportFragmentManager.findFragmentByTag(SetupDeviceViaSoftApFragment.class.getName());
        if (setupViaSoftApFragment != null && setupViaSoftApFragment.isVisible()) {
            MobileAppIntelligence.endOnboarding(EndData.createFailure("back_pressed_on_enter_creds"));
            showFragment(DisconnectFromSoftApLoadingFragment.newInstance(Prefs.SOFT_AP_SSID.getValue()), false);
            return true;
        }
        return false;
    }

    private boolean isBleSetupFragmentShowed(FragmentManager supportFragmentManager) {
        final Fragment setupViaBleFragment = supportFragmentManager.findFragmentByTag(SetupDeviceViaBluetoothFragment.class.getName());
        if (setupViaBleFragment != null && setupViaBleFragment.isVisible()) {
            MobileAppIntelligence.endOnboarding(EndData.createFailure("back_pressed_on_enter_creds"));
            showFragment(new HomeFragment(), false);
            return true;
        }
        return false;
    }

    @Override
    public void showFragment(Fragment fragment, boolean addToBackStack) {
        super.showFragment(fragment, addToBackStack);

        if (drawer != null) {
            if (fragment instanceof HomeFragment || fragment instanceof ConfigurationFragment ||
                    fragment instanceof OnBoardingHistoryFragment) {

                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            } else {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                switch (item.getItemId()) {
                    case R.id.nav_products:
                        showFragment(new HomeFragment(), false);
                        break;
                    case R.id.nav_history:
                        showFragment(new OnBoardingHistoryFragment(), false);
                        break;
                    case R.id.nav_configuration:
                        MobileAppIntelligence.enterStep(
                                StepData.create(
                                        StepResult.SUCCESS,
                                        "configuring",
                                        "moved_to_configuration"
                                )
                        );
                        showFragment(new ConfigurationFragment(), false);
                        break;
                    case R.id.nav_log_out:
                        logOut();
                        break;
                }

                drawer.removeDrawerListener(this);
            }
        });

        closeDrawer();

        return true;
    }

    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
}
