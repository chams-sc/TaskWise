package com.example.taskwiserebirth.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> focusModeLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> aiNameLiveData = new MutableLiveData<>();

    public LiveData<Boolean> getFocusModeLiveData() {
        return focusModeLiveData;
    }

    public void setFocusMode(Boolean isEnabled) {
        focusModeLiveData.setValue(isEnabled);
    }

    public LiveData<String> getAiNameLiveData() {
        return aiNameLiveData;
    }

    public void setAiName(String aiName) {
        aiNameLiveData.setValue(aiName);
    }
}
