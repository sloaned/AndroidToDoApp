package com.example.catalyst.androidtodo.network.RetrofitInterfaces;

import com.example.catalyst.androidtodo.models.Task;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ITask {

    @GET("task/{id}")
    Call<Task> getTask(@Path("id") int id);

    @GET("allTasks")
    Call<ResponseBody> getAllTasks();

    @DELETE("task/{id}")
    Call<ResponseBody> deleteTask(@Path("id") int id);

    @PUT("task")
    Call<ResponseBody> editTask(@Body Task task);

    @POST("task")
    Call<ResponseBody> createTask(@Body Task task);

}
