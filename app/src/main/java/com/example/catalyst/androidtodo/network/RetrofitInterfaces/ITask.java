package com.example.catalyst.androidtodo.network.RetrofitInterfaces;

import com.example.catalyst.androidtodo.models.Task;

import okhttp3.ResponseBody;
import retrofit2.Call;
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
    Boolean deleteTask(int id);

    @PUT("task")
    Boolean editTask(Task task);

    @POST("task")
    Call<Task> createTask(Task task);

}
