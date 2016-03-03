package com.example.catalyst.androidtodo.network.RetrofitInterfaces;

import com.example.catalyst.androidtodo.network.entities.LoginUser;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ILoginUser {

    @POST("login")
    Call<LoginUser> login(@Body LoginUser loginUser);
}
