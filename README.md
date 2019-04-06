# Android-project

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

For the the most advanced feature (detect pedestrians) we will use the phones camera app combined with the OpenCV library.

