package com.example.taskwiserebirth.conversational;

import android.util.Log;

import com.bin4rybros.demo.LAppModel;
import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SpeechSynthesis {

    private static final String voiceName = "en-US-SaraNeural";
    private static final String pitch = "high";
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static volatile boolean isShutdown = false;

    public static synchronized void initialize() {
        if (isShutdown) {
            executor = Executors.newSingleThreadExecutor();
            isShutdown = false;
        }
    }

    public static void synthesizeSpeechAsync(String text) {
        if (!isShutdown) {
            executor.submit(() -> synthesizeSpeech(text));
        } else {
            Log.w("SpeechSynthesis", "Attempted to submit task to a shutdown executor");
        }
    }

    private static void synthesizeSpeech(String text) {
        String speechKey = "75d247fa36164e01827df12050f000b5";
        String speechRegion = "southeastasia";

        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
        AudioConfig audioConfig = AudioConfig.fromDefaultSpeakerOutput();
        SpeechSynthesizer speechSynthesizer = new SpeechSynthesizer(speechConfig, audioConfig);

        speechSynthesizer.VisemeReceived.addEventListener((o, e) -> {
            // Update lip sync values based on the received viseme ID and audio offset
            LAppModel.updateLipSync(e.getVisemeId(), e.getAudioOffset());
        });

        try {
            String ssmlTemplate = loadSsmlTemplate();
            String ssml = String.format(ssmlTemplate, voiceName, pitch, text);
            Future<SpeechSynthesisResult> future = speechSynthesizer.SpeakSsmlAsync(ssml);
            SpeechSynthesisResult speechSynthesisResult = future.get();

            if (speechSynthesisResult.getReason() == ResultReason.SynthesizingAudioCompleted) {
                Log.i("SpeechSynthesis", "Speech synthesized for text: " + text);
            } else if (speechSynthesisResult.getReason() == ResultReason.Canceled) {
                SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(speechSynthesisResult);
                Log.i("SpeechSynthesis", "Speech synthesis canceled. Reason: " + cancellation.getReason());

                if (cancellation.getReason() == CancellationReason.Error) {
                    Log.e("SpeechSynthesis", "Speech synthesis error. ErrorCode: " + cancellation.getErrorCode());
                    Log.e("SpeechSynthesis", "Speech synthesis error details: " + cancellation.getErrorDetails());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.e("SpeechSynthesis", "Error occurred during speech synthesis", e);
        } finally {
            speechSynthesizer.close(); // Ensure the SpeechSynthesizer is closed
            speechConfig.close(); // Ensure the SpeechConfig is closed
            audioConfig.close(); // Ensure the AudioConfig is closed
        }
    }

    private static String loadSsmlTemplate() {
        return "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xml:lang=\"en-US\">\n" +
                "    <voice name=\"%s\">\n" +
                "        <prosody pitch=\"%s\">\n" +
                "            %s\n" +
                "        </prosody>\n" +
                "    </voice>\n" +
                "</speak>";
    }

    // Shutdown ExecutorService
    public static void shutdown() {
        isShutdown = true;
        executor.shutdown();
    }
}

