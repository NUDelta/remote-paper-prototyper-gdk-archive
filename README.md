prototyper
==========

prototyper is an application built for Google Glass to enable unique new types of interactions between users and developers, especially when building or exploring socially based technologies. This application means to more freely separates a user tester from the observer, while still retaining useful lines of communication between the two and collecting important data.

## Features
1. Stream video from Glass to web server.
2. Text communication from observer to Glass wearer.
3. Geolocation information of Glass wearer.
4. Audio communication from observer to Glass wearer**.

**Work-in-progress

## Usage
prototyper requires some sort of media server that will receive an RTSP stream (for example, [Wowza](http://www.wowza.com/)). The address of this server and a username/password must hardcoded into the CameraActivity section of the application before deploying to Glass (so naturally, a static server would be most preferred). Once this is done, open up the Glass application and choose "Connect" from the menu. The stream can then be viewed using the [command center webpage](https://github.com/NUDelta/prototype-cc).

## Example Setup
We tested our software setup on virtual machine running Red Hat Enterprise Linux with the aforementioned Wowza Media Server.

Installations instructions appropriate for the server are on the [Wowza website](http://www.wowza.com/) for a variety of operating systems. Wowza requires TCP port 1935 and UDP ports 6970-6978 open for communication with Glass. On our server, we used `iptables` to open the required ports.

````
sudo iptables -I INPUT -p tcp --dport 1935 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 6970 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 6971 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 6972 -j ACCEPT
...
````
And so on.

## File Setup
The application's files are setup much like a typical Android application. PrototypeActivity.java controls the main menu landing while CameraActivity.java handles the streaming interface and data communication activity.

## Technologies Used
This application uses the [libstreaming](https://github.com/fyhertz/libstreaming) library, while building heavily off of the [GlassStream](https://github.com/andermaco/GlassStream) github repository.

This application was mostly tested and built using a combination of Sublime Text 3 and Android-Eclipse.

## To-do List
* implement audio communication
* update logos and background

