package com.AndroidDriverImt3673.prosjekt;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

// Class to represent data that should be saved to Google Cloud Firestore for authenticated users.
// Use this class when a new trip is started, and when the trip is done to save statistics.
// Should only be used for authenticated users.


interface OnGetDataFromDBListener {                                 // Interface to retrieve data from
    void onSuccess(ArrayList<Trip> tripList);                       //  Firestore DB which runs as Async.
}

public class Trip {
    private String mUID;                                            // The users unique ID.
    private String mDate;                                           // Date when the trip started.
    private long mStartTime;                                        // Unix time of when the trip started.
    private long mEndTime;                                          // Unix time of when the trip stopped.
    private long mTotalTime;                                        // Time in seconds of how long the trip lasted.
    private int mKMsTravelled;                                      // KMs travelled during the trip.
    private int mAverageSpeed;                                      // Average speed in KMs for the trip.

    // Constructors:
    public Trip() {                                                 // Used by Firestore to create the trip.
        this.mUID = "";
        this.mDate = "";
        this.mStartTime = 0;
        this.mEndTime = 0;
        this.mTotalTime = 0;
        this.mKMsTravelled = 0;
        this.mAverageSpeed = 0;
    }

    public Trip(String uid) {                                       // Should be used when creating a new trip for a user.
        this.mUID = uid;
    }

    // Getters and Setters:
    public String getUID() {
        return mUID;
    }

    public void setUID(String uid) {
        this.mUID = uid;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        this.mStartTime = startTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(long endTime) {
        this.mEndTime = endTime;
    }

    public long getTotalTime() {
        return mTotalTime;
    }

    public void setTotalTime(long totalTime) {
        this.mTotalTime = totalTime;
    }

    public int getKMsTravelled() {
        return mKMsTravelled;
    }

    public void setKMsTravelled(int kmsTravelled) {
        this.mKMsTravelled = kmsTravelled;
    }

    public int getAverageSpeed() {
        return mAverageSpeed;
    }

    public void setAverageSpeed(int averageSpeed) {
        this.mAverageSpeed = averageSpeed;
    }

    // Saves a new trip for for a user to the database.
    public void saveTripToDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference collectionRef = db.collection(this.mUID);
        collectionRef.add(this)                                     // Creates a new document under the users collection.
                .addOnSuccessListener(documentReference ->          // Success listener.
                        Log.d("FirestoreData",
                                "DocumentSnapshot successfully written"))

                .addOnFailureListener(e ->                          // Failure listener.
                        Log.w("FirestoreData",
                                "Error writing document", e));
    }

    // Retrieves all trips for a user from the database.
    public void retrieveAllTripsFromDB(String docID, final OnGetDataFromDBListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference collectionRef = db.collection(docID);
        collectionRef.get().addOnCompleteListener(task -> {         // Complete listener.
            ArrayList<Trip> tripList = new ArrayList<>();           // List that should contain all trips for the user.
            if (task.isSuccessful()) {
                for (DocumentSnapshot doc : task.getResult()) {     // Loops through all documents.
                    Trip trip = doc.toObject(Trip.class);           // Converts the document to a Trip object.
                    tripList.add(trip);                             // Adds the trip to a list.
                }
            }
            listener.onSuccess(tripList);                           // Calls 'onSuccess' and adds the list.
        });
    }

    // Delete a trip for a user from the database.
    public void deleteTripFromDB(String documentID) {

    }
}
