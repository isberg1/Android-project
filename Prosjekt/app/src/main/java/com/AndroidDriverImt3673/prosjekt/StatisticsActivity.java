package com.AndroidDriverImt3673.prosjekt;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.lang.String.valueOf;


// ToDo: Remove this:
// Firebase Auth Tutorial: https://www.youtube.com/watch?v=EO-_vwfVi7c

// ToDo: Create statistics for the app.
// ToDo: Create a web-app to show the same statistics.

public class StatisticsActivity extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 7117;                    // Request number used as reference (Can be any number).
    List<AuthUI.IdpConfig> providers;                                   // List of sign in providers.
    TextView txtUserName;
    Button signOutBtn;
    Button testInsertDB;
    Button testRetrieveDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        txtUserName = findViewById(R.id.txtView_username);              // TextView to display username.
        signOutBtn = findViewById(R.id.btn_signOut);                    // Button to sign out.
        testInsertDB = findViewById(R.id.btn_insertDB);                 // ToDo: Remove DB Testing buttons.
        testRetrieveDB = findViewById(R.id.btn_retrieveDB);

        providers = Arrays.asList(                                      // Initiates supported sign-in providers.
                new AuthUI.IdpConfig.FacebookBuilder().build(),         // Facebook sign in.
                new AuthUI.IdpConfig.GoogleBuilder().build()            // Google sign in.
        );

        // Signs in user if not already signed in.
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {                            // If user is not signed in:
            showSignInOptions();                                        //   Calls func to allow user to sign in.
        } else {                                                        // If user is already signed in:
            updateActivityUI();                                         //   Calls function to update the rest of the UI.
        }

        // Button to sign out.
        signOutBtn.setOnClickListener(v -> {
            // Creates an listener for failures to sign out.
            AuthUI.getInstance()                                        // Signs the user out of the app.
                    .signOut(StatisticsActivity.this)
                    .addOnCompleteListener(task -> {
                        signOutBtn.setEnabled(false);                   // Disables the "sign-out" button.
                        Toast.makeText(this,
                                "Successfully signed out",
                                Toast.LENGTH_SHORT).show();
                        finish();                                       // Exit to MainActivity.
                    }).addOnFailureListener(e ->
                        Toast.makeText(StatisticsActivity.this,  // Display message if error in logout.
                            "Error: in onFailure for sign out " +
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show());
        });

        // Button to test insertion of data to the DB.
        testInsertDB.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            // Create a new trip object.
            Trip trip = new Trip(user.getUid());

            // Set the date for the trip.
            String datePattern = "dd-MM-yyyy";
            SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
            String date = dateFormat.format(new Date());
            trip.setDate(date);

            // Start time as unix time.
            long unixTime = System.currentTimeMillis() / 1000L;
            trip.setStartTime(unixTime);

            // End time as unix time (+ 1 hour).
            long tempEndTime = unixTime + 7200L;
            trip.setEndTime(tempEndTime);

            // Distance travelled.
            trip.setKMsTravelled(100);

            // Average speed.
            trip.setAverageSpeed(60);

            // Set the total time in seconds.
            trip.setTotalTime(trip.getEndTime() - trip.getStartTime());

            // Save to DB.
            trip.saveTripToDB();
        });

        // Button to test retrieval of data from DB.
        testRetrieveDB.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            // Retrieve data from DB.
            Trip trip = new Trip(user.getUid());
            trip.retrieveAllTripsFromDB(user.getUid(), new OnGetDataFromDBListener() {
                @Override
                public void onSuccess(ArrayList<Trip> tripList) {
                    Log.d("FirestoreData", "onSuccess list: " + tripList);

                    // ToDo: Call functions to work on the data.



                    LineChart mChart;
                    mChart = findViewById(R.id.line_graph);


                    mChart.setDragEnabled(true);
                    mChart.setScaleEnabled(false);


                    ArrayList<Entry> yValues = new ArrayList<>();

                    yValues.add(new Entry(0, 60));
                    yValues.add(new Entry(1, 50));
                    yValues.add(new Entry(2, 70));
                    yValues.add(new Entry(3, 30));
                    yValues.add(new Entry(4, 50));
                    yValues.add(new Entry(5, 60));
                    yValues.add(new Entry(6, 65));

                    LineDataSet set1 = new LineDataSet(yValues, "KMs travelled");

                    set1.setFillAlpha(110);

                    set1.setColor(Color.RED);
                    set1.setLineWidth(3);
                    set1.setValueTextSize(10);
                    set1.setValueTextColor(Color.BLACK);
                    set1.setCircleColor(Color.BLACK);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1);

                    LineData data = new LineData(dataSets);
                    mChart.setData(data);


                }
            });
        });
    }

    // Shows the sign in providers and allow user to sign into app.
    private void showSignInOptions() {
        startActivityForResult(                                     // Starts the FirebaseUI activity.
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers)
                    .setTheme(R.style.MyTheme)
                    .build(), MY_REQUEST_CODE
        );
    }

    // Function gets called when user authenticates for the first time.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in",
                        Toast.LENGTH_SHORT).show();
                updateActivityUI();                                 // Calls function to update the rest of the UI.
            } else {
                Toast.makeText(this, "Error response: "
                        + response.getError().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Updates the StatisticsActivity view.
    public void updateActivityUI() {
        String userName;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.getDisplayName() == null) {                        // Gets the username.
            userName = "Anonymous user";
        } else {
            userName = user.getDisplayName();
        }

        txtUserName.setText("Signed in as " + userName);            // Show username in activity.
        signOutBtn.setEnabled(true);                                // Enables the "sign out" button.
    }

    // ToDo: Add functions to create graphs for statistics.
}
