package com.example.catalyst.androidtodo.network.RetrofitInterfaces;

import com.example.catalyst.androidtodo.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface IUsers {

    @GET("user/{id}")
    Call<User> getUser(@Path("id") int id);

    @POST("login.json")
    Call<User> login(@Body User user);

    @POST("user")
    Call<User> postUser(@Body User user);
}
