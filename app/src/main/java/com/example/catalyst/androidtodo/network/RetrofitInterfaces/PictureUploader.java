package com.example.catalyst.androidtodo.network.RetrofitInterfaces;

import com.example.catalyst.androidtodo.models.ImageUpload;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PictureUploader {

    @POST("androidUpload")
    Call<ImageUpload> uploadImage(@Body ImageUpload imageUpload);
}
