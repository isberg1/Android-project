# Android-project - Android driving assistant

## App functionality
The app shows your driving speed, the speed limit on the street you are driving and the streets name.    
It uses the android location API to get the driving speed and Bing Maps API to get the speed limit and street name.    
The Bing Maps API is also used to display traffic incidents in an area around the user.

The app allows users to sign in with Google or Facebook. If the user signs in the app will record driving statistics and save them to the cloud.    
These statistics can then be viewed in the app in the form of bar graphs.    
For the authentication the app uses Google Firebase and Google Firestore for storing data in the database. 

The app aslo supports voice commands to take a picture using the camera.    
To do this voice must first be activtated, when the voice command "picture" is said, a picture is taken and saved to the phones gallery.      
It uses the camera2 API to take the picture and android.speech API to listen to voice commands.


### App layout:
![](app.png?raw=true)

*** 
## Original Idea
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
 

### Technical implementation 
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

***

## Project report
In this project we have made an android driving assistant app. It can find the street you are one, its speed limit and your current driving speed. It is able to record driving statistics and backing them up to Googles Firebase system. The app can also access online traffic rapports and display them. Finally the app is capable of listening for a voice command that’s. and upon speech recognition activation, and the command ‘picture’ will use the camera2 API to take a picture.


### Alexander Jakobsen
I made a speech recognition functionality which makes it possible to take a picture by voice command. To do this, I had to interact with the camera directly, as I was unable to use any other app to do this in the way I wanted it to be done. E.g. take a picture without leaving the activity and pushing a button. 
The options for doing this was the ‘camera’ and ‘camera2’ API. According to my study the ‘camera API’ was simple to use, but it had been deprecated since Android API 21. So, I did not want to use it. The ‘camera2 API’ on the other hand was much more difficult to use. But I did eventually manage to get it working. 

The speech recognition functionality is made by using the ‘android.speech API’. It listens for audio input, processing it and returns a string. I faced quite a lot of challenges tying to use this as a constantly running service. 

* You may only have 1 speech recognizer running at time. And it is only capable of either listening of possessing at a time, not both. For the user to be able to know if the app is ready for voice commands I made I field on the screen turn green when ready and red when it is processing.

* At activation and deactivation, the speech recognizer plays a beep sound. This would be extremely irritating for the user to constantly listen to. I could not find any way to deactivate it by configuration. Some entrees on stack Overflow claimed it wasn’t possible, because google made It this way so the user would be aware that sound was being recorded. So, I had to turn off the media sound at the start of the activity and turn it back on again afterwards. 

* To use the speech recognizer, you must have it installed on the phone and to use it local, you must have a language pack installed. My phone only has access to the English language pack. So, I could only make voice commands in English.

I also met some difficulties when using the camera2 API. I found several different examples on the internet. But none of them were any great. They worked for their own use case. But as soon as I tried to do anything else. Like rotating the screen or exiting unexpectantly. They stop working.  In the end we had to lock the activity orientation to vertical to ensure everything would work as desired. 

For the detect pedestrians in front of the camera. I was unable to implement it. But I started by tying to learn real time object detection for android, by using some demo projects I found 
https://www.youtube.com/watch?v=EhMrf4G5Wf0&feature=youtu.be   
https://github.com/natanielruiz/android-yolo 

but it did not work. Later I Found a google demo project to demonstrate how to user TensorFlow with camera, but it is very complicated. If I am to use it, it would primarily be to copy paste googles code. Possibly to make my own protobuffer object model. 
Google demo project url: 
https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android  

I eventually determined that this project was to advanced for me to use. Besides the camera2 API it used a lot of image processing I did not understand. I allso watched a lot of videoes about tensorflow form youtube channel :
https://www.youtube.com/channel/UCWN3xxRkmTPmbKwht9FuE5A/featured 


### Mats
I made the statistics functionality of the app. Connected the app to Google Firebase for authentication with Google and Facebook and to store statistics data in the Firestore database. To get Facebook login to work I had to create a Facebook app and connect it to Firebase. 

When a new trip is started and if the user is authenticated the trip is saved to Firestore. Each user has its own Collection of trips in the database. The collection has multiple documents, where each document corresponds to one "trip" (when a user presses "Start" to the user presses "Stop" in the app). In these documents the following are stored:    
 * Date
 * Start time
 * End time
 * Km's travelled
 * Time spent travelling

Two activities where created to select and view statistics. One activity where the user can select which "dates" they want to see stats for. When one or more "dates" are picked from the list, the seconds activity can be started that displays bar graphs generated from the data in Firestore. The bar graphs show stats for each date picked. A date can have multiple "trips", and in this Activity the user can also pick a date to show stats for each "trip" on that specific date. 

The hardest part was to make the bar graphs because I had to combine a lot of data to create the "stats for dates". This had to be done in a completely different way for "stats for each trips".

