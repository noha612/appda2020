package edu.ptit.vn.appda2020.module;

public class ApiUtils {

    public static final String BASE_URL = "http://192.168.43.11:8888/";

    private ApiUtils() {
    }

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
