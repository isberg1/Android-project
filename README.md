﻿# Android-project

## Idea
Android driving assistant. 

At its most basic it is able to 
*	display current driving speed
*	display the speed limit
*	display right of way
*	display street name
*	automatic day/night mode


at a more advanced level, it is able to
*	display traffic reports (snow, accidents)
*	driving statistics
*	configurable GUI (what elements are displayed + their size + themes)


the most advanced feature is the ability to detect pedestrians at risk of being hit by the car and to notify the driver before an accident occurs.
 

## Technical implementation 
For many of the basic features it may be possible to run google maps in the background and extract data tough its API. 

If traffic rapports cannot be extracted though google maps API. We will need to crate our own backend REST API and database with dummy data. 

For configuring the GUI, we may try to use interchangeable fragment. 

For the most advanced feature (detect pedestrians) we will use the phones camera app combined with the OpenCV library. A possible way to solve this is that we try to use the phones GPS/google maps API to determine the cars current position, its direction and speed. And combine this data with data form the camera to determine if the car is about to collide with something. For camera/OpenCV picture analysis, we may start by tying to identify the silhouettes of people. 
