package com.sampleapp.net.requester;

import android.content.Context;
import android.widget.Toast;

import com.sampleapp.net.RetrofitClient;
import com.sampleapp.net.model.LoginResponse;

public abstract class LoginRequester extends BaseRequester<LoginResponse> {
    private final Context context;

    public LoginRequester(Context context, String username, String password) {
        super(RetrofitClient.getCloudApi().login(username, password), context);
        this.context = context;
    }

    @Override
    public void onSuccess(LoginResponse response) {
        final int accountId = response.getAccountId();
        if (accountId == 0) {
            final String msg = "Can't get account ID";
            onFailure(msg, 0, "");
        } else {
            onSuccess(String.valueOf(accountId));
        }
    }

    @Override
    public void onFailure(String error, int statusCode, String errorBody) {
        if (statusCode == 401) {
            Toast.makeText(context, "Username or password is invalid", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
        }
    }

    public abstract void onSuccess(String accountId);
}
