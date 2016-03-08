package com.example.catalyst.androidtodo.listener;

import com.example.catalyst.androidtodo.event.LoginErrorEvent;
import com.example.catalyst.androidtodo.event.LoginSuccessEvent;

import org.greenrobot.eventbus.EventBus;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginListener implements Callback<ResponseBody> {

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        EventBus.getDefault().post(new LoginSuccessEvent(response));
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        EventBus.getDefault().post(new LoginErrorEvent(t));
    }
}
