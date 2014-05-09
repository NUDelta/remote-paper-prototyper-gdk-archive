prototyper
==========

prototyper is an application built for Google Glass to enable unique new types of interactions between users and developers, especially when building or exploring socially based technologies. This application means to more freely separates a user tester from the observer, while still retaining useful lines of communication between the two and collecting important data.

## Features
1. Stream video from Glass to web server.
2. Text communication from observer to Glass wearer.
3. Audio communication from observer to Glass wearer.
4. Geolocation information of Glass wearer.

NB: 2-4 are all currently WIP.

## Usage
prototyper requires some sort of media server that will receive an RTSP stream (for example, [Wowza](http://www.wowza.com/)). The address of this server and a username/password must hardcoded into the CameraActivity section of the application before deploying to Glass (so naturally, a static server would be most preferred). Once this is done, open up the Glass application and choose "Connect" from the menu. The stream can then be viewed using at [kevinjchen.com](kevinjchen.com).

## Technologies Used
This application uses the [libstreaming](https://github.com/fyhertz/libstreaming) library, while building heavily off of the [GlassStream](https://github.com/andermaco/GlassStream) github repository.

This application was mostly tested and built using a combination of Sublime Text 3 and Android-Eclipse.

## To-do List
* implement geolocation (probably with a Parse or Firebase backend)
* implement audio communication
* update logos and background
