package com.example.catalyst.androidtodo.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.catalyst.androidtodo.models.ImageUpload;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.models.User;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ILoginUser;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ITask;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.IUsers;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.PictureUploader;
import com.example.catalyst.androidtodo.network.entities.LoginUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiCaller {

    public static final String TAG = ApiCaller.class.getSimpleName();

    private static final String BASE_URL = "http://pc30120.catalystsolves.com:8080/";

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private OkHttpClient client = new OkHttpClient();
    private PictureUploader picUploader;
    private SharedPreferences prefs;
    private Retrofit retrofit;

    private ILoginUser loginUser;
    private ITask apiCaller;
    private IUsers userCall;

    public ApiCaller(Context context) {

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        client = assignInterceptor();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }

    private OkHttpClient assignInterceptor() {
        return client.newBuilder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        }).build();
    }

    public void makeTaskGetCall() {
        apiCaller = retrofit.create(ITask.class);
        Call<Task> taskGetCall = apiCaller.getTask(1);

        taskGetCall.enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, retrofit2.Response<Task> response) {
                Task newTask = response.body();

                Log.d(TAG, newTask.getTaskTitle());
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                Log.e(TAG, "Error: " + t.toString());
            }
        });
    }

    public void loginUserAndGetToken() {

        LoginUser loginRequest = new LoginUser();
        loginRequest.setUsername("random@gmail.com");
        loginRequest.setPassword("Password3");

        loginUser = retrofit.create(ILoginUser.class);
        Call<LoginUser> loginInfo = loginUser.login(loginRequest);

        loginInfo.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, retrofit2.Response response) {

                for (String head : response.headers().names()) {
                    Log.d(TAG, "Info: " + head + " " + response.headers().values(head));
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.e(TAG, "Dat Failure: " + t.getMessage());
            }
        });
    }

    public void makeUserPostCall() {
        User newUser = new User();
        newUser.setFirstName("Retrofit");
        newUser.setLastName("Android");
        newUser.setUsername("newUser@gmail.com");
        newUser.setPassword("password");

        userCall = retrofit.create(IUsers.class);
        Call<User> userPostCall = userCall.login(newUser);

        userPostCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, retrofit2.Response<User> response) {

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Failure: " + t.getMessage());
            }
        });
    }

    public void uploadImage(ImageUpload imageUpload){

        picUploader = retrofit.create(PictureUploader.class);
        Call<ImageUpload> imgPostCall = picUploader.uploadImage(imageUpload);

        imgPostCall.enqueue(new Callback<ImageUpload>() {
            @Override
            public void onResponse(Call<ImageUpload> call, retrofit2.Response<ImageUpload> response) {
                System.out.println("Picture uploaded: " + response.isSuccess());
            }

            @Override
            public void onFailure(Call<ImageUpload> call, Throwable t) {
                System.out.println("Image failed to upload to the server.... ");
            }
        });
    }

    private void setCookie(String cookie) {
        prefs.edit().putString("cookies", cookie).apply();
    }

    private String getCookie() {
        return prefs.getString("cookies", null);
    }
}
