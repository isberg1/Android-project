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

public class ShowStatisticsActivity extends AppCompatActivity {
    TextView totalTimeSpentTravelling;
    TextView totalKMsTravelled;
    BarChart chartTimeSpent;
    BarChart chartKmsTravelled;
    BarChart chartAverageSpeed;

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

            // Filter out trips that we done want.
            for (int i = 0; i < showStatsFordates.size(); i++) {        // Outer loop:
                for (int j = 0; j < tripList.size(); j++) {             // Inner loop:
                    if (showStatsFordates.get(i).equals(tripList.get(j).getDate())) {
                        markedTrips.add(tripList.get(j));               // Add the trip to the list of trips that should be displayed.
                    }
                }
            }

            // Make sure all bars have a date.
            ArrayList<String> labels = new ArrayList<>();
            for (int i = 0; i < markedTrips.size(); i++) {
                labels.add(markedTrips.get(i).getDate());
            }

            // Create the data set for the "time spent travelling" graph.
            ArrayList<BarEntry> timeSpentYvalues = new ArrayList<>();
            for (int i = 0; i < markedTrips.size(); i++) {
                // ToDo: Convert from seconds to minutes.
                timeSpentYvalues.add(new BarEntry(i, (int) markedTrips.get(i).getTotalTime()));
            }
            BarDataSet timeSpentSet = new BarDataSet(timeSpentYvalues, "Time spent travelling in minutes");
            createChart(chartTimeSpent, labels, timeSpentSet);

            // Create the data set for the "KMs travelled" graph.
            ArrayList<BarEntry> kmsTravelledYvalues = new ArrayList<>();
            for (int i = 0; i < markedTrips.size(); i++) {
                kmsTravelledYvalues.add(new BarEntry(i, markedTrips.get(i).getKMsTravelled()));
            }
            BarDataSet kmsTravelledSet = new BarDataSet(kmsTravelledYvalues, "KMs travelled");
            createChart(chartKmsTravelled, labels, kmsTravelledSet);

            // Create the data set for the "average speed" graph.
            ArrayList<BarEntry> averageSpeedYvalues = new ArrayList<>();
            for (int i = 0; i < markedTrips.size(); i++) {
                averageSpeedYvalues.add(new BarEntry(i, markedTrips.get(i).getAverageSpeed()));
            }
            BarDataSet averageSpeedSet = new BarDataSet(averageSpeedYvalues, "Average speed in KM/h");
            createChart(chartAverageSpeed, labels, averageSpeedSet);
        });
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
        chart.getAxisLeft().setEnabled(false);                          // Hide grid lines.
        chart.getAxisRight().setEnabled(false);                         // Hide grid lines.
        chart.setFitBars(true);                                         // Bars fit to screen.
        chart.setData(data);                                            // Display the chart.
        chart.invalidate();
    }
}
