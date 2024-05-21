package com.example.taskwiserebirth.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> focusModeLiveData = new MutableLiveData<>();

    public LiveData<Boolean> getFocusModeLiveData() {
        return focusModeLiveData;
    }

    public void setFocusMode(Boolean isEnabled) {
        focusModeLiveData.setValue(isEnabled);
    }
}
