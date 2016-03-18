package com.example.catalyst.androidtodo.event;

/**
 * Created by dsloane on 3/18/2016.
 */
public class ProgressBarShowEvent {
    private final boolean show;

    public ProgressBarShowEvent(boolean b) {
        show = b;
    }

    public boolean isShow() {
        return show;
    }
}
