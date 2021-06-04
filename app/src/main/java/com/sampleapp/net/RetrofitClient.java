package com.sampleapp.net;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.sampleapp.BuildConfig;
import com.sampleapp.CirrentApplication;

import java.security.GeneralSecurityException;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static final String TOKEN_BASE_URL = BuildConfig.API_PRODUCT_CLOUD_BASE_URL;
    private static ProductCloudApi CLOUD_API;

    public static ProductCloudApi getCloudApi() {
        if (CLOUD_API == null) {
            OkHttpClientBuilder builder = new OkHttpClientBuilder();
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(interceptor);
            }

            if (Build.VERSION.SDK_INT < 22) {
                enableTls12(builder);
            }

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(TOKEN_BASE_URL)
                    .client(builder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            CLOUD_API = retrofit.create(ProductCloudApi.class);
        }

        return CLOUD_API;
    }

    private static void enableTls12(OkHttpClientBuilder builder) {
        try {
            builder.enableTls12ForApiLower22();
        } catch (GeneralSecurityException e) {
            String msg = "Can't init TLS 1.2: check Google Play Services. " + e.getLocalizedMessage();
            Log.e("RetrofitClient", msg, e);
            Toast.makeText(CirrentApplication.getAppContext(), msg, Toast.LENGTH_LONG).show();
        }
    }
}
