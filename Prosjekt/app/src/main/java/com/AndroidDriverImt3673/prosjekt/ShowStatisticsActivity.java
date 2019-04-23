package com.AndroidDriverImt3673.prosjekt;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ShowStatisticsActivity extends AppCompatActivity {
    private TextView totalTimeSpentTravelling;
    private TextView totalKMsTravelled;
    private BarChart chartTimeSpent;
    private BarChart chartKmsTravelled;
    private BarChart chartAverageSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_statistics);

        totalTimeSpentTravelling = findViewById(R.id.text_total_time);
        totalKMsTravelled = findViewById(R.id.text_total_kms);
        chartTimeSpent = findViewById(R.id.graph_bar_timeSpent);
        chartKmsTravelled = findViewById(R.id.graph_bar_kms);
        chartAverageSpeed = findViewById(R.id.graph_bar_averageSpeed);

        // Gets the dates to show statistics for from StatisticsActivity.
        ArrayList<String> showStatsFordates = getIntent().getStringArrayListExtra(StatisticsActivity.DATE_LIST_NAME);

        // Get data from Firestore and filter it.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Trip trip = new Trip(user.getUid());
        trip.retrieveAllTripsFromDB(user.getUid(), tripList -> {
            ArrayList<Trip> markedTrips = new ArrayList<>();            // The trips to be displayed.

            // Calculate statistics for all trips.
            long timeSpentTravelling = 0;
            long kmsTravelled = 0;

            // Display statistics for all trips.
            for (int i = 0; i < tripList.size(); i++) {                 // Loops through all trips for a user.
                timeSpentTravelling += tripList.get(i).getTotalTime();
                kmsTravelled += tripList.get(i).getKMsTravelled();
            }
            HashMap<String, Long> timeMap = calculateTime(timeSpentTravelling);
            String totalTimeSpentString = timeMap.get("Days") + "d, "
                    + timeMap.get("Hours") + "h, "
                    + timeMap.get("Minutes") + "m, "
                    + timeMap.get("Seconds") + "s";
            totalTimeSpentTravelling.setText(totalTimeSpentString);

            String totalKMsString = kmsTravelled + " km";
            totalKMsTravelled.setText(totalKMsString);

            // Filter out trips that we don't want.
            for (int i = 0; i < showStatsFordates.size(); i++) {        // Outer loop: Loops through array sent from previous activity.
                for (int j = 0; j < tripList.size(); j++) {             // Inner loop: Loops through Trips array from Firestore.
                    if (showStatsFordates.get(i).                       // If date sent to this Activity is in one or more of the trips
                            equals(tripList.get(j).getDate())) {        //   from the Firestore DB, add the "trip" object to a new array.
                        markedTrips.add(tripList.get(j));               // Add the trip to the list of trips that should be displayed.
                    }
                }
            }

            // Create the graph for "time spent travelling".
            ArrayList<BarEntry> timeSpentYvalues = calculateBarValue(showStatsFordates, markedTrips, "TotalTime");
            BarDataSet timeSpentSet = new BarDataSet(timeSpentYvalues, "Time spent travelling in minutes");
            createChart(chartTimeSpent, showStatsFordates, timeSpentSet);

            // Create the graph for "KMs travelled".
            ArrayList<BarEntry> kmsTravelledYvalues = calculateBarValue(showStatsFordates, markedTrips, "KMsTravelled");
            BarDataSet kmsTravelledSet = new BarDataSet(kmsTravelledYvalues, "km's travelled");
            createChart(chartKmsTravelled, showStatsFordates, kmsTravelledSet);

            // Create the graph for "average speed".
            ArrayList<BarEntry> averageSpeedYvalues = calculateBarValue(showStatsFordates, markedTrips, "AverageSpeed");
            BarDataSet averageSpeedSet = new BarDataSet(averageSpeedYvalues, "Average speed in km/h");
            createChart(chartAverageSpeed, showStatsFordates, averageSpeedSet);
        });
    }

    // Converts seconds to days, hours, minutes and seconds.
    public HashMap calculateTime(long tripSeconds) {
        int days = (int) TimeUnit.SECONDS.toDays(tripSeconds);          // Gets days from seconds.
        long hours = TimeUnit.SECONDS.toHours(tripSeconds) -            // Gets the remaining hours.
                TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.SECONDS.toMinutes(tripSeconds) -        // Gets the remaining minutes.
                TimeUnit.DAYS.toMinutes(days) -
                TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.SECONDS.toSeconds(tripSeconds) -        // Gets the remaining seconds.
                TimeUnit.DAYS.toSeconds(days) -
                TimeUnit.HOURS.toSeconds(hours) -
                TimeUnit.MINUTES.toSeconds(minutes);

        HashMap<String, Long> timeMap = new HashMap<>();                // Adds the time in a hash table.
        timeMap.put("Days", (long) days);
        timeMap.put("Hours", hours);
        timeMap.put("Minutes", minutes);
        timeMap.put("Seconds", seconds);

        return timeMap;                                                 // Returns the hash table of time.
    }

    // Calculates a bars value and returns an ArrayList of BarEntry's.
    public ArrayList<BarEntry> calculateBarValue(ArrayList<String> dates, ArrayList<Trip> trips, String type) {
        ArrayList<BarEntry> yValues = new ArrayList<>();
        int value = 0;

        for (int i = 0; i < dates.size(); i++) {                        // Loops through the chosen dates.
            for (int j = 0; j < trips.size(); j++) {                    // Loops through all 'marked trips'
                if (dates.get(i).equals(trips.get(j).getDate())) {      //   and updates y value if the current Trip has the current date.
                    switch (type) {
                        case "TotalTime":                               // Gets the bar value when "total time" should be calculated.
                            value += (int) trips.get(j).getTotalTime() / 60;
                            break;
                        case "KMsTravelled":                            // Gets the bar value when "KMs travelled" should be calculated.
                            value += trips.get(j).getKMsTravelled();
                            break;
                        case "AverageSpeed":                            // Gets the bar value when "average speed" should be calculated.
                            value += trips.get(j).getAverageSpeed();
                            break;
                    }
                }
            }
            yValues.add(new BarEntry(i, value));                        // Add the bar entry to the y values array.
            value = 0;
        }

        return yValues;                                                 // Returns the ArrayList of BarEntry's.
    }

    // Creates a new chart.
    public void createChart(BarChart chart, ArrayList<String> labels, BarDataSet dataset) {
        // Configure the data set and create the data.
        dataset.setColors(ColorTemplate.MATERIAL_COLORS);
        dataset.setDrawValues(true);
        dataset.setValueTextColor(Color.BLACK);
        dataset.setValueTextSize(12);
        BarData data = new BarData(dataset);

        // Add labels to the chart and configure it.
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setLabelRotationAngle(50);                     // Rotation of labels.
        chart.getXAxis().setTextSize(11);                               // Size of label text.

        // Configure chart.
        chart.getDescription().setEnabled(false);                       // Disables description.
        chart.setDrawValueAboveBar(true);                               // Value is displayed above the bar.
        chart.animateXY(1000, 1000);             // Bars are sliding in from left to right.
        chart.getXAxis().setLabelCount(labels.size());                  // Add labels on all bars.
        chart.getAxisLeft().setEnabled(true);                           // Enable grid lines on the left side.
        chart.getAxisRight().setEnabled(false);                         // Hide grid lines on the right side.
        chart.setFitBars(true);                                         // Bars fit to screen.
        chart.setData(data);                                            // Display the chart.
        chart.invalidate();
    }
}
