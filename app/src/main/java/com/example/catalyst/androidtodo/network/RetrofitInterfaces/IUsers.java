package com.example.catalyst.androidtodo.network.RetrofitInterfaces;

import com.example.catalyst.androidtodo.models.User;
import com.example.catalyst.androidtodo.network.entities.LoginUser;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface IUsers {

    @GET("user/{id}")
    Call<User> getUser(@Path("id") int id);

    @GET("user/")
    Call<User> getUser();

    @DELETE("user/{id}")
    Boolean deleteUser(@Path("id") int id);

    @POST("login")
    Call<ResponseBody> login(@Body LoginUser login);

    @POST("login.json")
    Call<User> login(@Body User user);

    @POST("user")
    Call<ResponseBody> postUser(@Body User user);
}
