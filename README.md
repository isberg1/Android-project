# Android-project

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

