package edu.ptit.vn.appda2020.retrofit;

import android.content.Context;

import edu.ptit.vn.appda2020.R;

public class ApiUtils {


    private ApiUtils() {
    }

    public static APIService getAPIService(Context context) {
        String baseUrl = context.getString(R.string.server_url);
        return RetrofitClient.getClient(baseUrl).create(APIService.class);
    }
}
