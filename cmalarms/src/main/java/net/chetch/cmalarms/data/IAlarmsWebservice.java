package net.chetch.cmalarms.data;

import net.chetch.webservices.AboutService;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IAlarmsWebservice {
    String SERVICE_NAME = "Alarms";

    @GET("about")
    Call<AboutService> getAbout();
}
