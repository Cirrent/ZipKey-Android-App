package com.sampleapp.net.requester;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cirrent.cirrentsdk.CirrentProgressView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This class does basic logic of requesting data with Retrofit framework and error handling.
 * Extending classes are abstract and just provide Retrofit call interface in constructor.
 * <p>
 * The standard usage of children:
 * <pre>
 * <code>
 * new ChildRequester(parameters) {
 *     public void onSuccess(String result) {
 *     }
 *     public void onFailure(String error) {
 *     }
 * }.doRequest();
 * </code>
 * </pre>
 */
public abstract class BaseRequester<T> {

    private static final List<Call> cancelableCalls = new ArrayList<>();
    private final Call<T> call;
    private Context context;

    BaseRequester(Call<T> call, Context context) {
        this.call = call;
        this.context = context;
    }

    public void doRequest(final CirrentProgressView progressDialog) {
        if (context == null) {
            onFailure("Bad context", 0, "");
            return;
        }
        if (!isInternetConnected()) {
            onFailure("No internet connection", 0, "");
            return;
        }

        if (progressDialog != null) {
            progressDialog.showProgress();
        }

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (progressDialog != null) {
                    progressDialog.stopProgress();
                }

                if (call.isCanceled()) return;

                final int statusCode = response.code();
                if (response.isSuccessful()) {
                    final T body = response.body();
                    if (body != null) {
                        onSuccess(body);
                    } else {
                        BaseRequester.this.onFailure("Can't extract data response body.", statusCode, "");
                    }
                } else {
                    BaseRequester.this.onFailure("Server returned error: " + statusCode, statusCode, getStringifiedErrorBody(response.errorBody()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                if (progressDialog != null) {
                    progressDialog.stopProgress();
                }

                if (call.isCanceled()) return;

                BaseRequester.this.onFailure(t.getMessage(), 0, "");
            }
        });

        cancelableCalls.add(call);
    }

    public static void cancelAllCalls() {
        Log.i("BaseRequester", "Canceling " + cancelableCalls.size() + " calls");
        for (Call cancelableCall : cancelableCalls) {
            cancelableCall.cancel();
        }

        cancelableCalls.clear();
    }

    public abstract void onSuccess(T result);

    public abstract void onFailure(String error, int statusCode, String errorBody);

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String getStringifiedErrorBody(ResponseBody responseBody) {
        String errorBody = "";
        if (responseBody == null) return errorBody;
        try {
            errorBody = responseBody.string();
        } catch (IOException e) {
            Log.e("ERROR_STRINGIFIER", e.getMessage(), e);
        }

        return errorBody;
    }
}
