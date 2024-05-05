package com.example.taskwiserebirth.conversational;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TTSManager {
    // TODO: this is for sample testing only and will be replaced by Azure TTS later
    private TextToSpeech tts;

    public TTSManager(Context context, final TTSInitListener listener) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    listener.onInitSuccess();
                } else {
                    listener.onInitFailure();
                }
            }
        });
    }

    public interface TTSInitListener {
        void onInitSuccess();
        void onInitFailure();
    }

    public void setLanguageAndVoice(Locale locale, int voiceIndex) {
        tts.setLanguage(locale);
        Set<Voice> voices = tts.getVoices();
        List<Voice> voiceList = new ArrayList<>(voices);
        tts.setVoice(voiceList.get(voiceIndex));
    }

    public void setLanguageAndVoice() {
        Locale desiredLocale = Locale.US; // Change to the desired language/locale
        tts.setLanguage(desiredLocale);

        Set<Voice> voices = tts.getVoices();
        List<Voice> voiceList = new ArrayList<>(voices);
        Voice selectedVoice = voiceList.get(0); // Change to the desired voice index
        tts.setVoice(selectedVoice);
    }

    public void convertTextToSpeech(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}
