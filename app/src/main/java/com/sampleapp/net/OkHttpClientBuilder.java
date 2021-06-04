package com.sampleapp.net;

import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;

class OkHttpClientBuilder {

    private static final String TAG = OkHttpClientBuilder.class.getSimpleName();
    private final OkHttpClient.Builder builder;

    OkHttpClientBuilder() {
        this.builder = new OkHttpClient.Builder();
    }

    OkHttpClientBuilder addInterceptor(HttpLoggingInterceptor interceptor) {
        builder.addInterceptor(interceptor);
        return this;
    }


    OkHttpClient build() {
        return builder.build();
    }

    OkHttpClientBuilder enableTls12ForApiLower22() throws GeneralSecurityException {
        X509TrustManager x509TrustManager = generateX509TrustManager();
        TLSSocketFactory sslSocketFactory = new TLSSocketFactory(getTlsSocketFactory());
        builder.sslSocketFactory(sslSocketFactory, x509TrustManager);
        builder.connectionSpecs(Collections.singletonList(getConnectionSpecs()));
        return this;
    }

    private SSLSocketFactory getTlsSocketFactory()
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        return context.getSocketFactory();
    }

    private X509TrustManager generateX509TrustManager() throws GeneralSecurityException {
        String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(defaultAlgorithm);
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        if (trustManagers.length == 1 && trustManagers[0] instanceof X509TrustManager) {
            return (X509TrustManager) trustManagers[0];
        }
        String msg = " Can't create X509TrustManager: Unexpected default trust managers: ";
        throw new java.security.GeneralSecurityException(TAG + msg + Arrays.toString(trustManagers));
    }

    private ConnectionSpec getConnectionSpecs() {
        return new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
                        CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256,
                        CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
                        CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256
                )
                .build();
    }
}
