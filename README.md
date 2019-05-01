﻿# Android-project

## Idea
Android driving assistant. 

At its most basic the app is able to:    
*	Display current driving speed
*	Display the speed limit
*	Display right of way
*	Display street name
*	Automatic switch between day/night mode
* Register and login with facebook, google, etc


At a more advanced level, the app is able to:    
*	Display traffic reports (snow, accidents)
*	Configurable GUI (what elements are displayed + their size + themes)
*	Driving statistics
* Find car after it has been parked

The most advanced feature is the ability to detect pedestrians at risk of being hit by the car and to notify the driver before an accident occurs.
 

## Technical implementation 
For many of the basic features it may be possible to run google maps in the background and extract data tough its API. 

Statistics can be saved in firebase. Statistics can be saved for:    
 * How long the trip lasted (hours, mins).
 * Average and max speed.
 * Km's travelled.
 * Calculate how "agressive" or "passive" the driver is (advanced feature).

If traffic rapports cannot be extracted though google maps API. We will need to crate our own backend REST API and database with dummy data. 

For configuring the GUI, we may try to use interchangeable fragment. 

For the most advanced feature (detect pedestrians) we will use the phones camera app combined with the OpenCV library. A possible way to solve this is that we try to use the phones GPS/google maps API to determine the cars current position, its direction and speed. And combine this data with data form the camera to determine if the car is about to collide with something. For camera/OpenCV picture analysis, we may start by tying to identify the silhouettes of people. 

![alt text](https://github.com/isberg1/Android-project/blob/master/Activity_design_Idea.png "")


## Project rapport
In this project we have made an android driving assistant app. It can find the street you are one, its speed limit and your current driving speed. It is able to record driving statistics and backing them up to Googles Firebase system. The app can also access online traffic rapports and display them. Finally the app is capable of listening for a voice command that’s. and upon speech recognition activation, and the command ‘picture’ will use the camera2 API to take a picture.

### Alexander Jakobsen
I made a speech recognition functionality which makes it possible to take a picture by voice command. To do this, I had to interact with the camera directly, as I was unable to use any other app to do this in the way I wanted it to be done. E.g. take a picture without leaving the activity and pushing a button. 
The options for doing this was the ‘camera’ and ‘camera2’ API. According to my study the ‘camera API’ was simple to use, but it had been deprecated since Android API 21. So, I did not want to use it. The ‘camera2 API’ on the other hand was much more difficult to use. But I did eventually manage to get it working. 

The speech recognition functionality is made by using the ‘android.speech API’. It listens for audio input, processing it and returns a string. I faced quite a lot of challenges tying to use this as a constantly running service. 

* You may only have 1 speech recognizer running at time. And it is only capable of either listening of possessing at a time, not both. For the user to be able to know if the app is ready for voice commands I made I field on the screen turn green when ready and red when it is processing.

* At activation and deactivation, the speech recognizer plays a beep sound. This would be extremely irritating for the user to constantly listen to. I could not find any way to deactivate it by configuration. Some entrees on stack Overflow claimed it wasn’t possible, because google made It this way so the user would be aware that sound was being recorded. So, I had to turn off the media sound at the start of the activity and turn it back on again afterwards. 

* To use the speech recognizer, you must have it installed on the phone and to use it local, you have to have a language pack installed. My phone only has access to the English language pack. So, I could only make voice commands in English.


### Mats
I made the driving statistics functionality of the app where the driver can see graphs of their driving data. I also connected the app to Google Firebase to allow users to log into the app with Google and Facebook. I had to add a Facebook app to allow Firebase access to Facebook. The driving data is stored in Google Firestore database. 

Only authenticated users are allowed to store data to the Firestore DB. When a new trip is startet in the app, data like how long the trips lasted, average speed, date, km's travelled is stored in the DB. The "Statistics" Activity gets all the data from Firestore and displays the date the trip was started. The user can then pick one ore more of these dates and display different kind of statistics in graphs. 

3 different "main" graphs are created for all the "trips" on the dates picked. The user can also pick a single date from a drop down menu that creates graphs for all "trips" on the chosen date. 