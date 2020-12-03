package edu.ptit.vn.appda2020.retrofit;

import edu.ptit.vn.appda2020.model.dto.Direction;
import edu.ptit.vn.appda2020.model.dto.Location;
import edu.ptit.vn.appda2020.model.dto.Place;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIService {

    @GET("/da2020/v1/places")
    Call<Place[]> getPlaces(@Query("name") String name);

    @GET("/da2020/v1/directions")
    Call<Direction> getDirections(@Query("from-id") String fromId, @Query("to-id") String toId);

    @GET("/da2020/v1/locations")
    Call<Location> getLocations(@Query("lat") String fromId, @Query("lng") String toId);
}
