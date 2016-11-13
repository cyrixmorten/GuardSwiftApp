package com.guardswift.rest;

import com.guardswift.BuildConfig;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public final class GuardSwiftWeb {


    public static final String API_URL = BuildConfig.WEB_API_URL;

    public interface API {

        @GET("version")
        Call<ResponseBody> version();

        @GET("download")
        Call<ResponseBody> download();

    }


}
