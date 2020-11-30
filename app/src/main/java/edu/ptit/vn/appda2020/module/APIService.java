package edu.ptit.vn.appda2020.module;

import edu.ptit.vn.appda2020.model.Place;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIService {

    @GET("/da2020/v1/places")
    Call<Place[]> getPlaces(@Query("name") String name);
}
