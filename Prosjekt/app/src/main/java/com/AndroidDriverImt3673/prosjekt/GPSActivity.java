package com.AndroidDriverImt3673.prosjekt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class GPSActivity extends Activity implements GPSListener  {
    private TextView textView;
    private RequestQueue que;
    public double longi = 0;
    public double lati = 0;
    public static String SEND_LONG = "longi";
    public static String SEND_LAT = "lati";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        Button btnI = findViewById(R.id.btnI);

        btnI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putDouble(SEND_LONG ,longi);
                bundle.putDouble(SEND_LAT, lati);
                final Intent startIncident = new Intent(GPSActivity.this, IncidentsActivity.class);
                startIncident.putExtras(bundle);
                startActivity(startIncident);
            }
        });

        Button btnAPI = findViewById(R.id.btnAPI);
        que = Volley.newRequestQueue(this);
        btnAPI.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendAPI();
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        } else {
            startApp();
            sendAPI();
        }

        this.updateSpeed(null);
    }

    public void sendAPI(){
        textView = findViewById(R.id.txtAPIRes);
        String url = "https://dev.virtualearth.net/REST/v1/Routes/SnapToRoad?points=" + lati + "," + longi + "&IncludeSpeedLimit=true&speedUnit=KPH&key=AhloF-tCKXkUy1HBgDXp9xljOoebG6BzAAJz0xu8xtDbojMFFIxew7DokDbp5nfe";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
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
                            for (int k = 0; k< jsonArray2.length(); k++) {
                                JSONObject jsonObject2 = jsonArray2.getJSONObject(k);
                                String speedLimit = jsonObject2.getString("speedLimit");
                                String speedUnit = jsonObject2.getString("speedUnit");

                                String name = jsonObject2.getString("name");
                                textView.setText(name + "\n Has speed limit " + speedLimit + " " + speedUnit);

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

        que.add(request);
    }

    @SuppressLint("SetTextI18n")
    private void updateSpeed(CLocation location) {
        float CurrentSpeed = 0;

        if(location != null)
        {
            int speed=(int) (location.getSpeed()*(3600/1000));
            CurrentSpeed = speed;

        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", CurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        String strUnits = "km/h";

        TextView txtCurrentSpeed = this.findViewById(R.id.txtCurrentSpeed);
        txtCurrentSpeed.setText(strCurrentSpeed + " " + strUnits);
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

        TextView txtAddress = this.findViewById(R.id.txtAdd);
        txtAddress.setText(address);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startApp();
            } else {
                finish();
            }
        }
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

