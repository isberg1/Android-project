package com.AndroidDriverImt3673.prosjekt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IncidentsActivity extends AppCompatActivity {
    public double longi = 0;
    public double lati = 0;
    private RequestQueue que;
    public List<Incident> incidents = new ArrayList<>();
    public static RecyclerView recyclerView;
    public static IncidentAdapter adapter;
    public static RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);
        recyclerView = findViewById(R.id.rcrView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        que = Volley.newRequestQueue(this);
        Intent i = this.getIntent();
        longi = i.getDoubleExtra(GPSActivity.SEND_LONG, 0);
        lati = i.getDoubleExtra(GPSActivity.SEND_LAT, 0);
        sendAPI();

    }


    public void sendAPI(){
        String url = "https://dev.virtualearth.net/REST/v1/Traffic/Incidents/" +  (int)lati + "," + (int)longi + "," + (int)(lati +1) + "," + (int)(longi +1) + "?key=AhloF-tCKXkUy1HBgDXp9xljOoebG6BzAAJz0xu8xtDbojMFFIxew7DokDbp5nfe\n";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("resourceSets");
                    for (int i = 0; i< jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONArray jsonArray1 = jsonObject.getJSONArray("resources");
                        for (int j = 0; j< jsonArray1.length(); j++){
                            JSONObject jsonObject1 = jsonArray1.getJSONObject(j);
                            String desc = jsonObject1.getString("description");
                            incidents.add(new Incident(desc));
                            adapter = new IncidentAdapter(incidents,IncidentsActivity.this);
                            recyclerView.setAdapter(adapter);
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
}
