package com.AndroidDriverImt3673.prosjekt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CallBack, GPSListener {
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


    //Martins testvaribler
    private RequestQueue que;
    public double longi = 0;
    public double lati = 0;
    public static String SEND_LONG = "longi";
    public static String SEND_LAT = "lati";
    public Button btnTrip;
    public double tripStartLongitude;
    public double tripStartLatitude;
    public double tripEndLongitude;
    public double tripEndLatitude;
    public long tripStartTime;
    public long tripEndTime;
    public double tripAverageSpeed;
    public double tripKMTravelled;
    public String tripDate;
    private boolean tripActive = false;
    private TextView speedLim;
    private TextView streetName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView = findViewById(R.id.texture);

        if (!isOrientationPortrait(this)) {
            textureView.setRotation(90);
        }


        speakButton = (Button) findViewById(R.id.btn_speak);
        mText = (TextView) findViewById(R.id.mText);
        errorView1 = findViewById(R.id.errorView1);
        listener = Listener.getListener(this, mText, errorView1);


        // The request code used in ActivityCompat.requestPermissions()
// and returned in the Activity's onRequestPermissionsResult()
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_CONTACTS,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.CAMERA
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        assert textureView != null;
        cameraClass = new CameraClass(this, textureView);

        textureView.setSurfaceTextureListener(cameraClass.textureListener);


        btnTrip = findViewById(R.id.button_start);
        btnTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tripActive == false) {
                    startTrip();
                } else {
                    endTrip(tripStartLatitude, tripStartLongitude, tripStartTime);
                    tripActive = false;

                    btnTrip.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_start));
                }

            }
        });
        //  Button btnAPI = findViewById(R.id.btnAPI);
        que = Volley.newRequestQueue(this);
        //btnAPI.setOnClickListener(new View.OnClickListener(){
        //  @Override
        // public void onClick(View v) {
        //   sendAPI();
        //}
        //});
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        } else {
            startApp();
            sendAPI();
        }
        this.updateSpeed(null);


    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


   /* private void checkPermissions() {
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

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

    }
*/

    /**
     * start and stops the voice recognition
     * @param view the button that was pushed
     */
    public void startListeningBtn(View view) {
        Log.d(TAG, "startListeningBtn: " + listener.getIsRunning());
        if (listener.getIsRunning()) {
            listener.stop();
            mText.setText("");

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

            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startApp();
                    sendAPI();
                } else {
                    return;
                }
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
        mText.setText("");
        errorView1.setText("");
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

    public void GoToIncident(View view) {
        listener.stop();
        Bundle bundle = new Bundle();
        bundle.putDouble(SEND_LONG, longi);
        bundle.putDouble(SEND_LAT, lati);
        final Intent startIncident = new Intent(MainActivity.this, IncidentsActivity.class);
        startIncident.putExtras(bundle);
        startActivity(startIncident);
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
    public boolean isOrientationPortrait(Context context) {
        final int screenOrientation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
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


    public void startTrip() {
        tripActive = true;
        tripStartLatitude = lati;
        tripStartLongitude = longi;
        tripStartTime = System.currentTimeMillis() / 1000L;
        String datePattern = "dd-MM-yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
        tripDate = dateFormat.format(new Date());

        btnTrip.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_stop));

        /*if (tripActive) {
            btnTrip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    endTrip(tripStartLatitude, tripStartLongitude, tripStartTime);
                    tripActive = false;

                    btnTrip.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_start));
                }
            });
        }*/
    }


    public void endTrip(double startLat, double startLong, long startTime) {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
            };

            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }


            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        try {
            tripEndLongitude = location.getLongitude();
            tripEndLatitude = location.getLatitude();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        tripEndTime = System.currentTimeMillis() / 1000L;

        // FOR PRESENTATION PURPOSES, DO NOT REMOVE!
        String url = "https://dev.virtualearth.net/REST/v1/Routes/DistanceMatrix?origins=47.6044,-122.3345&destinations=45.5347,-122.6231&travelMode=driving&key=AhloF-tCKXkUy1HBgDXp9xljOoebG6BzAAJz0xu8xtDbojMFFIxew7DokDbp5nfe";



        JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("resourceSets");
                    for (int i = 0; i< jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONArray jsonArray1 = jsonObject.getJSONArray("resources");
                        for (int j = 0; j< jsonArray1.length(); j++) {
                            JSONObject jsonObject1 = jsonArray1.getJSONObject(j);
                            JSONArray jsonArray2 = jsonObject1.getJSONArray("results");
                            for (int k = 0; k< jsonArray2.length(); k++) {
                                JSONObject jsonObject2 = jsonArray2.getJSONObject(k);
                                tripKMTravelled = jsonObject2.getDouble("travelDistance");
                                double Duration = jsonObject2.getDouble("travelDuration");
                                tripAverageSpeed = (tripKMTravelled / (Duration * 0.0166666667));
                                saveToFireBase(tripDate,tripStartTime, tripEndTime, tripKMTravelled, tripAverageSpeed);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        que.add(request2);
    }

    public void saveToFireBase(String tripDate, long tripStartTime, long tripEndTime, double tripKMTravelled, double tripAverageSpeed) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Trip trip = new Trip(auth.getCurrentUser().getUid());

            trip.setDate(tripDate);

            trip.setStartTime(tripStartTime);

            trip.setEndTime(tripEndTime);

            int distance = (int)tripKMTravelled;
            trip.setKMsTravelled(distance);

            int avgSpeed = (int) tripAverageSpeed;
            trip.setAverageSpeed(avgSpeed);

            trip.setTotalTime(trip.getEndTime() - trip.getStartTime());
            trip.saveTripToDB();
            Toast.makeText(this, "Trip successfully saved", Toast.LENGTH_LONG).show();
            btnTrip.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    startTrip();
                }
            });
        }
        else {
            return;
        }

    }


    public void sendAPI(){
        speedLim = findViewById(R.id.speedLimit);
        //streetName = findViewById(R.id.textview_street_name);
        String url = "https://dev.virtualearth.net/REST/v1/Routes/SnapToRoad?points=" + lati + "," + longi + "&IncludeSpeedLimit=true&speedUnit=KPH&key=AhloF-tCKXkUy1HBgDXp9xljOoebG6BzAAJz0xu8xtDbojMFFIxew7DokDbp5nfe";

        JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("resourceSets");
                    for (int i = 0; i< jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONArray jsonArray1 = jsonObject.getJSONArray("resources");
                        for (int j = 0; j< jsonArray1.length(); j++) {
                            JSONObject jsonObject1 = jsonArray1.getJSONObject(j);
                            JSONArray jsonArray2 = jsonObject1.getJSONArray("snappedPoints");
                            for (int k = 0; k < jsonArray2.length(); k++) {

                                JSONObject jsonObject2 = jsonArray2.getJSONObject(k);
                                String speedLimit = jsonObject2.getString("speedLimit");
                                String speedUnit = jsonObject2.getString("speedUnit");


                                String name = jsonObject2.getString("name");
                                speedLim.setText(speedLimit);
                                //streetName.setText(name);

                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        que.add(request2);
    }

    @SuppressLint("SetTextI18n")
    private void updateSpeed(CLocation location) {
        float CurrentSpeed = 0;

        if(location != null)
        {
           // int speed=(int) (location.getSpeed()*(3600/1000));
            float speed = location.getSpeed();
            CurrentSpeed = speed;

        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", CurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        String strUnits = "km/h";

        // Removes decimal values and leading 0s in string.
        String roundedCurrentSpeed = strCurrentSpeed.split("\\.")[0];
        roundedCurrentSpeed = roundedCurrentSpeed.replaceFirst("^0+(?!$)", "");

        TextView txtCurrentSpeed = this.findViewById(R.id.drivingSpeed);
        txtCurrentSpeed.setText(roundedCurrentSpeed);
    }


    @Override
    public void onLocationChanged(Location location) {
        if(location != null)
        {
            try {
                UpdateAddress(location.getLatitude(), location.getLongitude());
            } catch (IOException e) {
                e.printStackTrace();
            }
            CLocation myLocation = new CLocation(location);
            this.updateSpeed(myLocation);
            longi = location.getLongitude();
            lati = location.getLatitude();
            sendAPI();
        }
    }

    private void UpdateAddress(double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

        streetName = this.findViewById(R.id.textview_street_name);
        streetName.setText(address);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @SuppressLint("MissingPermission")
    private void startApp() {
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        Toast.makeText(this, "GPS connection success", Toast.LENGTH_LONG).show();
    }

}