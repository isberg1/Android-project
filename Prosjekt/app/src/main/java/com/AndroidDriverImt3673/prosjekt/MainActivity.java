package com.AndroidDriverImt3673.prosjekt;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements CallBack {
    private static final String TAG = "MainActivity";

    private TextView mText;
    private TextView errorView1;
    //private SpeechRecognizer speechRecognizer;
    private Listener listener;
    private Button speakButton;
    boolean isActivated = false;


    private Button takePictureButton;
    private TextureView textureView;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private CameraClass cameraClass;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView = findViewById(R.id.texture);

        if (!isOrientationPortrait(this)){
            textureView.setRotation(90);
        }


        speakButton = (Button) findViewById(R.id.btn_speak);
        mText = (TextView) findViewById(R.id.mText);
        errorView1 = findViewById(R.id.errorView1);
        listener = Listener.getListener(this, mText, errorView1);
//        listener = new Listener(this, mText, errorView1);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        527);
            }
        }


        assert textureView != null;
        cameraClass = new CameraClass(this, textureView);

        textureView.setSurfaceTextureListener(cameraClass.textureListener);
        /*
        takePictureButton = findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraClass.takePicture();
            }
        });
        */
    }


    /**
     * start and stops the voice recognition
     * @param view the button that was pushed
     */
    public void buttonClick(View view) {
        Log.d(TAG, "buttonClick: " + listener.getIsRunning());
        if (listener.getIsRunning()) {
            listener.stop();

        } else {
            listener.start();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 527: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (listener == null) {
            listener = Listener.getListener(this, mText, errorView1);
        }
        listener.startSpeechRecognizer();
    }


    @Override
    protected void onResume() {
        super.onResume();
        listener.setListener();
        if (listener.getIsRunning()) {
            listener.recognize();
        }


        cameraClass.startBackgroundThread();
        if (textureView.isAvailable()) {
            cameraClass.openCamera();
        } else {
            textureView.setSurfaceTextureListener(cameraClass.textureListener);
        }


    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        //closeCamera
        cameraClass.stopBackgroundThread();
        listener.stopSpeechRecognizer();
        super.onPause();


    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listener.destroySpeechRecognizer();
        listener = null;
        Log.d(TAG, "onDestroy: ");
    }

    /**
     * callback function
     * is called form listener class
     * makes camera take 1 picture
     */
    @Override
    public void take1Picture() {
        cameraClass.takePicture();
    }

    public void GoToGPS(View view) {
        listener.stop();
        final Intent startGPS  = new Intent(MainActivity.this, GPSActivity.class);
        startActivity(startGPS);
    }

    public void GoToStats(View view) {
        listener.stop();
        Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
        startActivity(intent);
    }


    /**
     * finds the current screen orientation
     * @param context
     * @return true if Portrait, false if not
     */
    public boolean isOrientationPortrait(Context context){
        final int screenOrientation = ((WindowManager)  context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        switch (screenOrientation) {
            case Surface.ROTATION_0:
                return true;
            case Surface.ROTATION_90:
                return false;
            case Surface.ROTATION_180:
                return true;
            default:
                return false;
        }
    }

    /**
     * stops the speech recogniser correctly
     */
    @Override
    public void onBackPressed() {
        listener.stop();
        moveTaskToBack(true);
    }

}