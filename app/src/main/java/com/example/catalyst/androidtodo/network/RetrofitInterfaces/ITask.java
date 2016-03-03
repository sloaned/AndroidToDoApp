package com.example.catalyst.androidtodo.network.RetrofitInterfaces;

import com.example.catalyst.androidtodo.models.Task;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ITask {

    @GET("task/{id}")
    Call<Task> getTask(@Path("id") int id);
}
