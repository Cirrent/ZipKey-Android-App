package com.sampleapp;

import android.app.ProgressDialog;
import android.content.Context;

import androidx.core.content.ContextCompat;

import com.cirrent.cirrentsdk.CirrentProgressView;

public class SimpleProgressDialog implements CirrentProgressView {
    private ProgressDialog progressDialog;

    public SimpleProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setIndeterminateDrawable(ContextCompat.getDrawable(context, R.drawable.progress_indicator_yellow));
    }

    public SimpleProgressDialog(Context context, int messageResId) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(context.getString(messageResId));
        progressDialog.setIndeterminateDrawable(ContextCompat.getDrawable(context, R.drawable.progress_indicator_yellow));
    }

    @Override
    public void showProgress() {
        progressDialog.show();
    }

    @Override
    public void stopProgress() {
        progressDialog.dismiss();
    }
}
