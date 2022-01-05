package com.sampleapp.net.requester;

import android.content.Context;
import android.util.Log;

import com.sampleapp.Prefs;
import com.sampleapp.net.RetrofitClient;
import com.sampleapp.net.model.Token;

public abstract class MAITokenRequester extends BaseRequester<Token> {
    private Context context;

    protected MAITokenRequester(Context context, String encodedCredentials) {
        super(RetrofitClient.getCloudApi().getAnalyticsToken(encodedCredentials), context);
        this.context = context;
    }

    @Override
    public void onSuccess(Token result) {
        final String token = result.getToken();
        Log.i("MAITokenRequester", "MAI Token received: " + token);
        Prefs.TOKEN.setValue(token);
        onSuccess(token);
    }

    @Override
    public void onFailure(String error, int statusCode, String errorBody) {
        Log.e("AnalyticsTokenRequester", error);
    }

    public abstract void onSuccess(String analyticsToken);

}
