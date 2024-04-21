package com.arnichem.arnichem_barcode.view;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class RootClass implements LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START) public void start() {
        Log.d("dinesh", "start: ");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP) public void stop() {
        Log.d("dinesh", "stop: ");

    }
}
