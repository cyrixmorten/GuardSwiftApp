package com.guardswift.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by cyrixmorten on 17/04/16.
 */
public final class GuardSwift {


    public static final String API_URL = "http://www.guardswift.com/api/";

    public interface API {

        @GET("version")
        Call<ResponseBody> version();

        @GET("download")
        Call<ResponseBody> download();
    }


}
