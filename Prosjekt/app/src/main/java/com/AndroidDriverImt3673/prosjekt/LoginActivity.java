package com.AndroidDriverImt3673.prosjekt;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;


// ToDo: Remove this:
// Firebase Auth Tutorial: https://www.youtube.com/watch?v=EO-_vwfVi7c

public class LoginActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 7117;                    // Request number used as reference (Can be any number).
    List<AuthUI.IdpConfig> providers;
    TextView txtUserName;
    Button signOutBtn;
    Button testDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtUserName = findViewById(R.id.txtView_username);              // TextView to display username.
        signOutBtn = findViewById(R.id.btn_signOut);                    // Button to sign out.
        testDb = findViewById(R.id.btn_db);

        providers = Arrays.asList(                                      // Initiates supported sign-in providers.
                //new AuthUI.IdpConfig.AnonymousBuilder().build(),        // Anonymous builder.
                new AuthUI.IdpConfig.FacebookBuilder().build(),         // Facebook builder.
                new AuthUI.IdpConfig.GoogleBuilder().build()            // Google builder.
        );

        showSignInOptions();                                            // Calls func to show the different sign-in options.

        signOutBtn.setOnClickListener(v -> {                            // Listener for "sign-out" button.
            AuthUI.getInstance()                                        // Signs the user out of the app.
                    .signOut(LoginActivity.this)
                    .addOnCompleteListener(task -> {
                        signOutBtn.setEnabled(false);                   // Disables the "sign-out" button.
                        showSignInOptions();                            // Calls func to Show the sign-in options again.
                    }).addOnFailureListener(new OnFailureListener() {   // Creates an listener for failures to sign out.
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this,           // Display message if error in logout.
                            "Error: in onFailure for sign out " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // ToDo: Remove this temp object.
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

    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers)
                    .setTheme(R.style.MyTheme)
                    .build(), MY_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                String userName;

                // Get user.
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
            } else {
                Toast.makeText(this, "Error response: "
                        + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
