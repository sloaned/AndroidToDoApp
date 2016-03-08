package com.example.catalyst.androidtodo.event;

public class LoginErrorEvent {
    private final Throwable mThrowable;

    public LoginErrorEvent(Throwable t) {
        mThrowable = t;
    }

    public Throwable getThrowable() {
        return mThrowable;
    }
}
