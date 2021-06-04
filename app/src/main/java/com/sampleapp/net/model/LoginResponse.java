package com.sampleapp.net.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private int status;
    @SerializedName("data")
    private UserData userData;

    private class UserData {
        private User user;
    }

    private class User {
        @SerializedName("idAccount")
        private int accountId;
    }

    public int getAccountId() {
        int accountId = 0;
        if (userData != null && userData.user != null) {
            accountId = userData.user.accountId;
        }

        return accountId;
    }
}
