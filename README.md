tom131313 changes:<br>
Added multi-threading server.  Multiple clients are allowed.<br>
Added JPG quality/compression level option.<br>
Removed duplicate image to JPG encoding.<br>
Refactored a bit for the multiple client capability.<br>
Included a demo OpenCV drawing on the camera image before it is served (a filled circle and 2 polygons).<br>
Tried to retain the original coding but it looks a lot different when the multi-client is added and the redundancies removed.<br>
I'm a Java synchronization and OpenCV Mat novice.  Syncing the read and write of the camera image and waiting for a new image to serve might be right but maybe not. The OpenCV continuous Mat was attempted.
It seems to work right but that's no proof.<br>
For the synchronizing ReadWrite locks the default fairness was used along with the operating system thread scheduling.<br>
ToDo could be perform the Mat to JPG bytes encoding once in the capture/draw thread so it doesn't have to be done in each client server thread.<br>
_______________________________________
English | [Türkçe](./README.tr-TR.md)

<div align="center">

<img width="100" src="./static/logo.png"/>

<h1 align="center">HTTP Live Video Stream From OpenCV</h1>

</div>


OpenCV is a sample project that is read from the video source (Camera, File System, NVR, DVR etc.) and displays the processed image via the http protocol. OpenCV version 3.1 is used. You can see the architecture of the project below.


### Architecture

<div align="center">
<img width="500" src="./static/mimari.png"/>
</div>


### Dependency

 - OpenCV 3+
 - Java 6+


### How to run

If you use as follows
```sh
System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
```
set JVM parameter
```sh
-Djava.library.path=/yourpath/opencv-3.4.0/build/lib
```
or just use it
```sh
#for mac or linux
System.loadLibrary("/yourpath/opencv-3.4.0/build/lib");

#for windows
System.loadLibrary("/yourpath/opencv_java3xx");
```

