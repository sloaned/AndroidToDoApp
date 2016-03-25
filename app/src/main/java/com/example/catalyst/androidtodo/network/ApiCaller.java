package com.example.catalyst.androidtodo.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.catalyst.androidtodo.activities.MainActivity;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.models.User;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ILoginUser;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ITask;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.IUsers;
import com.example.catalyst.androidtodo.network.entities.LoginUser;
import com.example.catalyst.androidtodo.util.NetworkConstants;
import com.example.catalyst.androidtodo.util.SharedPreferencesConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiCaller {

    public static final String TAG = ApiCaller.class.getSimpleName();

    private static final String BASE_URL = "http://pc30120.catalystsolves.com:8080/";

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private static OkHttpClient client = new OkHttpClient();
    private static SharedPreferences prefs;

    private SharedPreferences.Editor mEditor;
    private Retrofit retrofit;

    private ILoginUser loginUser;
    private ITask apiCaller;
    private IUsers userCall;

    private Context mContext;

    private boolean loggedIn;
    private String userToken;

    public static OkHttpClient assignInterceptorWithToken() {

        final String token = prefs.getString(SharedPreferencesConstants.PREFS_TOKEN, (String) null);
        Log.d(TAG, "before adding interceptor, token = " + token);
        return client.newBuilder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .method(original.method(), original.body())
                        .header("X-AUTH-TOKEN", token)
                        .build();
                return chain.proceed(request);
            }
        }).build();
    }

    public ApiCaller(Context context) {

        mContext = context;



        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = prefs.edit();

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

   /* public OkHttpClient assignInterceptorWithToken() {

        final String token = prefs.getString(SharedPreferencesConstants.PREFS_TOKEN, (String) null);
        Log.d(TAG, "before adding interceptor, token = " + token);
        return client.newBuilder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .method(original.method(), original.body())
                        .header("X-AUTH-TOKEN", token)
                        .build();
                return chain.proceed(request);
            }
        }).build();
    }
*/
    public void getAllTasks() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = assignInterceptorWithToken();
       // client.addInterceptor(logging);
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        apiCaller = retrofit.create(ITask.class);
        Call<ResponseBody> getTasks = apiCaller.getAllTasks();
        Log.v(TAG, "here's the call: " + getTasks.toString());

        getTasks.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                try {
                    Log.v(TAG, response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (String head : response.headers().names()) {
                    Log.v(TAG, head + " " + response.headers().values(head));
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error: " + t.toString());
            }
        });
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

    public String loginUserAndGetToken(String username, String password) {

        LoginUser loginRequest = new LoginUser();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        loggedIn = false;

        loginUser = retrofit.create(ILoginUser.class);
        Call<ResponseBody> loginInfo = loginUser.login(loginRequest); //<LoginUser>

        loginInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                int i = 0;

                List<String> tokenVals = response.headers().values(NetworkConstants.TOKEN_HEADER_VALUE);
                Log.d(TAG, "here are the tokenVals: ");
                String token = "";
                for (String s : tokenVals) {
                    Log.d(TAG, s);
                    token += s;
                }
                Log.d(TAG, "Adding token to sharedPrefs, token = " + token);
                userToken = token;
                mEditor.putString(SharedPreferencesConstants.PREFS_TOKEN, token).apply();

                for (String head : response.headers().names()) {
                    Log.d(TAG, "Info: " + head + " " + response.headers().values(head));
                    if (head.equals(NetworkConstants.TOKEN_HEADER_VALUE)) {
                        Log.d(TAG, "got the token");

                        loggedIn = true;

                        Intent intent = new Intent(mContext, MainActivity.class);
                        mContext.startActivity(intent);
                    }
                    i++;
                } 

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Dat Failure: " + t.getMessage());
                userToken = "";
            }
        });
        Log.d(TAG, "logged in? : " + loggedIn);
        //return loggedIn;

        return userToken;
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


    private void setCookie(String cookie) {
        prefs.edit().putString("cookies", cookie).apply();
    }

    private String getCookie() {
        return prefs.getString("cookies", null);
    }
}
