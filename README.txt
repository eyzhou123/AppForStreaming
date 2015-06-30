README

SETUP:
You will need to download OpenCV. For Macs, the below link is a good tutorial to follow:
http://www.learnopencv.com/install-opencv-3-on-yosemite-osx-10-10-x/

You will also need to download the javacv-0.11-bin.zip file from the following link:
https://github.com/bytedeco/javacv
as this contains all the .jar files needed for building the app.

You will need to install ffmpeg for your computer to merge audio and video files. You can use macports/homebrew/apt-get or just download the binary from the internet.



CHANGES TO MAKE TO THE CODE BEFORE RUNNING:
Change IP Address:
Although you can change the IP Address in the yahoo app itself, its easier to hardcode the default to your own IP address. To do this, change the "server_ip" variable in the ReaderMainActivity.java file if you are using the YahooNewsApp. If you choose to run the test app (AndroidImageTest), you will need to change the "mIP" variable in MainActivity.java. 

Change path variables:
Since we will be saving videos, you will also need to edit the "path" variable in the SocketServer.java file, for example, to your own Desktop or directory. You also should change the "path_to_ffmpeg" variable in MainProgram to the absolute path of your ffmpeg binary file. To figure out what this is, type "which ffmpeg" into a terminal. 



HOW TO RUN:
To run this program, first run the MainProgram in OpenCVTest (not a good name for now). Confirm that a server window pops up with a blue button on the left side, and blank space on the right (the android stream will go here). Then, run the android app (either the test app AndroidImageTest or the YahooNewsApp). 