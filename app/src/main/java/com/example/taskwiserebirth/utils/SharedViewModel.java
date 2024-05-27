package com.example.taskwiserebirth.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> focusModeLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> aiNameLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> clearMemoryLiveData = new MutableLiveData<>();


    public LiveData<Boolean> getFocusModeLiveData() {
        return focusModeLiveData;
    }

    public void setAssistiveMode(Boolean isEnabled) {
        focusModeLiveData.setValue(isEnabled);
    }

    public LiveData<String> getAiNameLiveData() {
        return aiNameLiveData;
    }

    public void setAiName(String aiName) {
        aiNameLiveData.setValue(aiName);
    }

    public LiveData<Boolean> getClearMemoryLiveData() {
        return clearMemoryLiveData;
    }

    public void setClearMemory(boolean shouldClear) {
        clearMemoryLiveData.setValue(shouldClear);
    }

}
