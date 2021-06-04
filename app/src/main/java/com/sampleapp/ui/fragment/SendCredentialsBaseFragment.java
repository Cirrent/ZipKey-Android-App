package com.sampleapp.ui.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cirrent.cirrentsdk.CirrentProgressView;
import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.sampleapp.R;
import com.sampleapp.Utils;

public abstract class SendCredentialsBaseFragment extends BaseFullScreenFragment {
    protected static final String DEVICE_ID = "deviceId";
    protected static final String SELECTED_NETWORK = "selectedNetwork";
    protected static final String HIDDEN_NETWORK = "hiddenNetwork";
    protected static final String PRE_SHARED_KEY = "preSharedKey";

    protected boolean isHiddenNetwork;
    protected String deviceId;
    protected WiFiNetwork selectedNetwork;
    protected String preSharedKey;
    protected LocalCirrentProgressView progressView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_send_credentials_loading, container, false);
        Utils.startConstantVerticalViewAnimation(view.findViewById(R.id.logo));

        TextView textConnectingStatus = (TextView) view.findViewById(R.id.text_connecting_to);
        progressView = new LocalCirrentProgressView(textConnectingStatus);
        changeActionBarState(true, false, "");

        return view;
    }

    public class LocalCirrentProgressView implements CirrentProgressView {

        private final TextView textView;
        private String text;

        LocalCirrentProgressView(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void showProgress() {
            textView.setText(text);
        }

        @Override
        public void stopProgress() {
            textView.setText("");
        }

        public LocalCirrentProgressView withText(String text) {
            this.text = text;
            showProgress();
            return this;
        }

        public LocalCirrentProgressView withText(int id) {
            this.text = getString(id);
            showProgress();
            return this;
        }
    }
}
