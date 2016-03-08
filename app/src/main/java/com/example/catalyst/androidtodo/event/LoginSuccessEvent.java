package com.example.catalyst.androidtodo.event;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class LoginSuccessEvent {

    private final Response<ResponseBody> response;

    public LoginSuccessEvent(Response<ResponseBody> response) {
        this.response = response;
    }

    public Response<ResponseBody> getResponse() {
        return response;
    }
}
