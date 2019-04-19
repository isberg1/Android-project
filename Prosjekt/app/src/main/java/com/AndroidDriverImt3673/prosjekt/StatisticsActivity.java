package com.AndroidDriverImt3673.prosjekt;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.lang.String.valueOf;


// ToDo: Remove this:
// Firebase Auth Tutorial: https://www.youtube.com/watch?v=EO-_vwfVi7c

// ToDo: Only show 'Sign in' if user is not already signed into the app.
// ToDo: Check that the signed in user is persistent.
// ToDo: Add "Greetings" text over 'Sign in' boxes.
// ToDo: Each user must have a unique identifier to be used when writing and retrieving data.
// ToDo: Create statistics for the app.
// ToDo: Create a web-app to show the same statistics.

public class StatisticsActivity extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 7117;                    // Request number used as reference (Can be any number).
    List<AuthUI.IdpConfig> providers;
    TextView txtUserName;
    Button signOutBtn;
    Button testDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        txtUserName = findViewById(R.id.txtView_username);              // TextView to display username.
        signOutBtn = findViewById(R.id.btn_signOut);                    // Button to sign out.
        testDb = findViewById(R.id.btn_db);

        providers = Arrays.asList(                                      // Initiates supported sign-in providers.
                //new AuthUI.IdpConfig.AnonymousBuilder().build(),        // Anonymous sign in.
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

        // Sing out button listener.
        signOutBtn.setOnClickListener(v -> {
            AuthUI.getInstance()                                        // Signs the user out of the app.
                    .signOut(StatisticsActivity.this)
                    .addOnCompleteListener(task -> {
                        signOutBtn.setEnabled(false);                   // Disables the "sign-out" button.
                        Toast.makeText(this, "Successfully signed out", Toast.LENGTH_SHORT).show();
                        finish();                                       // Exit to MainActivity.
                    }).addOnFailureListener(new OnFailureListener() {   // Creates an listener for failures to sign out.
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(StatisticsActivity.this,           // Display message if error in logout.
                            "Error: in onFailure for sign out " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // ToDo: Remove this temp object.
        // Create som fake data.
        // Creates a trip to be saved to the database.
        testDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Gets the date.
                String datePattern = "dd-MM-yyyy";
                SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
                String date = dateFormat.format(new Date());

                // Create a new trip object.
                Trip trip = new Trip(date);

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
                trip.saveToDB();
            }
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
                Toast.makeText(this, "Successfully signed in", Toast.LENGTH_SHORT).show();
                updateActivityUI();                                 // Calls function to update the rest of the UI.
            } else {
                Toast.makeText(this, "Error response: "
                        + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Updates the StatisticsActivity view.
    public void updateActivityUI() {
        String userName;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Gets the username.
        if (user.getDisplayName() == null) {
            userName = "Anonymous user";
        } else {
            userName = user.getDisplayName();
        }

        // Show username in activity.
        txtUserName.setText("Signed in as " + userName);

        // Enables the "sign out" button.
        signOutBtn.setEnabled(true);
    }
}
