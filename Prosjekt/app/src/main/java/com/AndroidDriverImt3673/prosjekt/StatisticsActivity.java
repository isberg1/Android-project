package com.AndroidDriverImt3673.prosjekt;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// ToDo: Remove this:
// Firebase Auth Tutorial: https://www.youtube.com/watch?v=EO-_vwfVi7c

// ToDo: Create statistics for the app.
// ToDo: Create a web-app to show the same statistics.

public class StatisticsActivity extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 7117;                    // Request number used as reference (Can be any number).
    public static final String DATE_LIST_NAME = "DATE_LIST";            // Name ref to string array that is passed with the Intent.
    List<AuthUI.IdpConfig> providers;                                   // List of sign in providers.
    TextView txtUserName;
    ListView datesList;
    Button showStatsBtn;
    Button signOutBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        txtUserName = findViewById(R.id.txtView_username);              // TextView to display username.
        datesList = findViewById(R.id.trip_dates_list);                 // List of dates to show stats for.
        showStatsBtn = findViewById(R.id.btn_show_stats);               // Button to show statistics.
        signOutBtn = findViewById(R.id.btn_signOut);                    // Button to sign out.

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

        /*
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
        */
    }

    // Shows the sign in providers and allow user to sign into app.
    private void showSignInOptions() {
        startActivityForResult(                                         // Starts the FirebaseUI activity.
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
                updateActivityUI();                                     // Calls function to update the rest of the UI.
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

        if (user.getDisplayName() == null) {                            // Gets the username.
            userName = "Anonymous user";
        } else {
            userName = user.getDisplayName();
        }

        txtUserName.setText("Signed in as " + userName);                // Show username in activity.
        signOutBtn.setEnabled(true);                                    // Enables the "sign out" button.

        // Retrieves all data from Firestore for the current user, but only uses the dates.
        ArrayList<String> arrayOfDates = new ArrayList<>();
        Trip trip = new Trip(user.getUid());
        trip.retrieveAllTripsFromDB(user.getUid(), tripList -> {
            Log.d("FirestoreData", "onSuccess list: " + tripList);

            // Populate the ListView with unique dates from the DB.
            for (int i = 0; i < tripList.size(); i++) {
                if (!arrayOfDates.contains(tripList.get(i).getDate())) {
                    arrayOfDates.add(tripList.get(i).getDate());
                }
            }

            // Adds array of dates to "ListView" with an adapter.
            ArrayAdapter<String> adapter = new ArrayAdapter<>(StatisticsActivity.this,
                    android.R.layout.simple_list_item_1, arrayOfDates);
            datesList.setAdapter(adapter);

            // Mark one or more items in the list.
            ArrayList<String> pickedDates = new ArrayList<>();
            datesList.setOnItemClickListener((parent, view, position, id) -> {
                if (view.getTag() != null) {                            // If item has not been marked before.
                    view.setBackgroundColor(Color.WHITE);
                    view.setTag(null);
                    pickedDates.remove(                                 // Removes the un-picked date from the list.
                            datesList.getItemAtPosition(position).toString());
                } else {
                    view.setBackgroundColor(Color.LTGRAY);              // If item has been marked before.
                    view.setTag("selected");
                    pickedDates.add(                                    // Adds the picked date to a new list.
                            datesList.getItemAtPosition(position).toString());
                }

                if (pickedDates.size() != 0) {                          // Enable "Show Stats" button of one ore more dates have been picked.
                    showStatsBtn.setEnabled(true);
                } else {
                    showStatsBtn.setEnabled(false);
                }

                showStatsBtn.setOnClickListener(v -> {                  // Listener for the "Show stats" button.
                    Intent intent = new Intent(StatisticsActivity.this, ShowStatisticsActivity.class);
                    intent.putExtra(DATE_LIST_NAME, pickedDates);       // Sends the dates that was picked to Activity.
                    startActivity(intent);                              // Starts the Activity.
                });
            });
        });
    }
}
