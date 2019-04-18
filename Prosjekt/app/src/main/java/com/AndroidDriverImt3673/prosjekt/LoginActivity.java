package com.AndroidDriverImt3673.prosjekt;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 7117;    // Can be any number. ToDo: Change this.
    List<AuthUI.IdpConfig> providers;
    Button signOutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        signOutBtn = findViewById(R.id.btn_signOut);


        // Init providers.
        providers = Arrays.asList(
                new AuthUI.IdpConfig.AnonymousBuilder().build(), // Anonymous builder.
                new AuthUI.IdpConfig.FacebookBuilder().build(),  // Facebook builder.
                new AuthUI.IdpConfig.GoogleBuilder().build()     // Google builder.
        );

        showSignInOptions();

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logout.
                AuthUI.getInstance()
                        .signOut(LoginActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                signOutBtn.setEnabled(false);
                                showSignInOptions();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
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
                // Get user.
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                // Show toast.
                Toast.makeText(this, "User: " + user, Toast.LENGTH_SHORT).show();

                // Set button signout.
                signOutBtn.setEnabled(true);
            } else {
                Toast.makeText(this, "Response: " + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
