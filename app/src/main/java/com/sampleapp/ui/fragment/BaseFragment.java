package com.sampleapp.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sampleapp.BuildConfig;
import com.sampleapp.CirrentApplication;
import com.sampleapp.analytics.AnalyticsService;
import com.sampleapp.ui.FragmentListener;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseFragment extends Fragment {
    private AnalyticsService analyticsService;
    private FragmentListener mListener;

    protected final Handler deviceBinderHandler = new Handler();

    @Override
    public void onPause() {
        super.onPause();
        deviceBinderHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            mListener = (FragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentListener");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        analyticsService = new AnalyticsService(getContext());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void showFragment(Fragment fragment, boolean addToBackstack) {
        if (mListener != null) {
            mListener.showFragment(fragment, addToBackstack);
        }
    }

    public void changeActionBarState(boolean actionBarShouldBeHidden, boolean enableBackArrow, String title) {
        mListener.changeActionBarState(actionBarShouldBeHidden, enableBackArrow, title);
    }

    public void showToast(int resId, int duration) {
        Toast.makeText(CirrentApplication.getAppContext(), resId, duration).show();
    }

    public void showToast(String msg, int duration) {
        Toast.makeText(CirrentApplication.getAppContext(), msg, duration).show();
    }

    public AnalyticsService getAnalyticsService() {
        return analyticsService;
    }

    protected Map<String, String> getAppVersion() {
        final Map<String, String> appVersion = new HashMap<>();
        appVersion.put("app_version", BuildConfig.VERSION_NAME);
        return appVersion;
    }
}
