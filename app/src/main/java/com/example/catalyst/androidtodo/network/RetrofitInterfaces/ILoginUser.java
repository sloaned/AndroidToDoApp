package com.example.catalyst.androidtodo.network.RetrofitInterfaces;

import com.example.catalyst.androidtodo.network.entities.LoginUser;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ILoginUser {

    @POST("login")
    Call<ResponseBody> login(@Body LoginUser loginUser);
}
