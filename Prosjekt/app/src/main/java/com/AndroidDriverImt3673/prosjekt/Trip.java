package com.AndroidDriverImt3673.prosjekt;

import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Trip {
    private String mDate;
    private long mStartTime;
    private long mEndTime;
    private long mTotalTime;
    private int mKMsTravelled;
    private int mAverageSpeed;

    // Constructor:
    public Trip(String date) {
        this.mDate = date;
    }


    // Getters and Setters:
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


    // Saves all data to the database.
    // ToDo: This should be done for each unique user.
    public void saveToDB() {
        // Init Cloud Firestore.
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("trips")
                .add(this)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("FirebaseMessage", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FirebaseMessage", "Error adding document", e);
                    }
                });

        // Reads data back from firebase.
        db.collection("trips")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("FirebaseMessage", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("FirebaseMessage", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}
