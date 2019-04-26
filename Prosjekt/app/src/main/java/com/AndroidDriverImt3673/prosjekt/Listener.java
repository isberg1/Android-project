package com.AndroidDriverImt3673.prosjekt;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * source: https://stackoverflow.com/questions/6316937/how-can-i-use-speech-recognition-without-the-annoying-dialog-in-android-phones
 */
class Listener implements RecognitionListener {
    private static final String TAG = "Listener";

    private boolean isRunning = false;
    private TextView errorView1;
    private TextView mText;
    private Context context;
    private Activity activity;
    private SpeechRecognizer speechRecognizer;
    // interface used to get master/mother class to take a picture
    private CallBack takePicture;

    private boolean stopLitening = false;

    private Integer original_volume_level;
    private AudioManager audioManager;

    public void setStopLitening(boolean bool){ stopLitening = bool; }
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * constructor
     * @param context of the activity that called it
     * @param mText used for debugging
     * @param errorView1 used for debugging
     */
    public Listener(Context context,  TextView mText, TextView errorView1) {
        this.context = context;
        this.mText = mText;
        this.errorView1 = errorView1;
        this.activity = (Activity) context;
        this.speechRecognizer = null;
        this.takePicture = (CallBack) context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }


    /**
     * restarts the speech recognition
     * @param error
     */
    public void onError(int error)
    {
        Log.d(TAG,  "error " +  error);
        String text = getErrorText(error);
        errorView1.setText("error " + text);
        // set flag
        setIsRunning(false);
        // restarts the speech recognition


        if (!stopLitening) {
            recognize();
        }
    }

    /**
     * extract the string results  sent by the
     * speech recognition service/app/activity
     * @param results
     */
    public void onResults(Bundle results)
    {
        String str = new String();
        Log.d(TAG, "onResults " + results);
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < data.size(); i++)
        {
            Log.d(TAG, "result " + data.get(i));
            str += data.get(i) +  " , ";
        }
        mText.setText("results: "+str);
        // parse result
        processResult(data);
        // set flag
        setIsRunning(false);
        // restarts the speech recognition
        recognize();

    }

    /**
     * processResult a list of strings and call apropriate
     * helper-functions to do respective jobs
     * @param data commands
     */
    private void processResult(ArrayList<String > data) {
        // parse strings
        for (String entry : data) {
            // take a picture
            if (entry.toLowerCase().contains("picture")){
                errorView1.setText("it contains picture");
                takePicture.take1Picture();
                Log.d(TAG, "processResult: take 1 picture");

            } else if (entry.toLowerCase().contains("record")) {
                if (entry.toLowerCase().contains("start")) {
                    errorView1.setText("it contains start record");
                } else if (entry.toLowerCase().contains("stop")) {
                    errorView1.setText("it contains stop record");
                }
            } else if (entry.toLowerCase().contains("listening")) {
                // close app
                if (entry.toLowerCase().contains("stop")) {
                    errorView1.setText("stopping Speech Recognizer");
                    Activity activity = (Activity) context;
                    // close app
                    stopLitening= true;
                    speechRecognizer.cancel();
                    stopSpeechRecognizer();
                    //activity.finish();
                }
            }
        }
    }


    /**
     *  starts the speech recognition
     */
    public synchronized void  recognize() {
        errorView1.setBackgroundColor(activity.getResources().getColor(R.color.colorAccent));

        speechRecognizer.cancel();

        new Thread(new Runnable() {
            @Override
            public void run() {

                // prevent 2 instances of speech recognition form running at the same time
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // starts the speech recognition
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // set flag
                        setIsRunning(true);

                        errorView1.setBackgroundColor(activity.getResources().getColor(R.color.white));

                        // configure speech recognition
                        Intent intent = configureSpeechRecognition();
                        // starts speech recognition
                        if (!stopLitening) {
                            speechRecognizer.startListening(intent);
                            Log.d(TAG, "run: in recognize");
                        }
                    }
                });
            }
        }).start();
    }

    /**
     *  configure speech recognition
     * @return
     */
    private Intent configureSpeechRecognition() {
        String languagePref = "en-US";

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE , true);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languagePref);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languagePref);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languagePref);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

        return intent;
    }

    /**
     * possess error codes by convening int to text
     * @param errorCode int error type
     * @return error message
     */
    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                // error 3
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                // error 5
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                // error 9
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                // error 2
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                // error 1
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                // error 7
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                // error 8
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                // error 4
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                // error 6
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }




    public boolean getIsRunning() {
        return isRunning;
    }

    public void setIsRunning(boolean bool) {
        isRunning = bool;
    }

    public void startSpeechRecognizer() {
        original_volume_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
    }

    public void stopSpeechRecognizer() {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, original_volume_level, 0);
        speechRecognizer.stopListening();
    }

    public void destroySpeechRecognizer() {
        speechRecognizer.destroy();
    }
    public void setListener() {
        speechRecognizer.setRecognitionListener(this);
    }


    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "onReadyForSpeech");
        setIsRunning(true);
    }

    public void onBeginningOfSpeech()
    {
        Log.d(TAG, "onBeginningOfSpeech");
    }
    public void onRmsChanged(float rmsdB)
    {
        Log.d(TAG, "onRmsChanged");
    }
    public void onBufferReceived(byte[] buffer)
    {
        Log.d(TAG, "onBufferReceived");
    }
    public void onEndOfSpeech(){Log.d(TAG, "onEndOfSpeech"); }
    public void onPartialResults(Bundle partialResults)
    {
        Log.d(TAG, "onPartialResults");
    }
    public void onEvent(int eventType, Bundle params)
    {
        Log.d(TAG, "onEvent " + eventType);
    }

}