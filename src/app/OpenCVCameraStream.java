package app;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import editImage.ImageEdit;

public class OpenCVCameraStream extends Thread {
 
    public class LastChanceHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Server.mainThread.interrupt(); // bad to be here so tell the main to give it up
            System.out.println("last chance in thread " + t);
            e.printStackTrace();
            
        }
    }

    protected static Thread cameraThread = Thread.currentThread();
  
    private static VideoCapture videoCapture;
    protected static Mat image = new Mat(); // shared; lock ReadWrite
    private static Mat frame = new Mat(); // internal; sync copy to image when completed
    private static Mat tempImage = new Mat();
    protected static int frameCount = 0;
    protected static Object aLock = new Object();

    public void run() {

        Thread.setDefaultUncaughtExceptionHandler(new LastChanceHandler());

        // open requested camera
        videoCapture = new VideoCapture();
        System.out.println("Opening camera " + Server.camera);
        videoCapture.open(Server.camera); // camera number
        if (!videoCapture.isOpened()) {
            System.exit(1);
        }

        // grab camera frames until requested to stop
        while(!Thread.currentThread().isInterrupted()) {

            if (!videoCapture.read(frame)) {
                break;
                }

            try {

            // call any frame editing that someone may have put into the classpath
            tempImage = ImageEdit.edit(frame); // mess with the frame before serving it
            
            } catch(Exception ex){ex.printStackTrace(); Server.mainThread.interrupt();System.exit(1);}

            //processed frame complete; copy to synced image for others to view
            Server.lockImage.writeLock().lock();
            image = tempImage.clone();
            frameCount++;
            Server.lockImage.writeLock().unlock();
            synchronized(aLock){aLock.notifyAll();}
        }

        // interrupted so quit
        System.exit(1);
    }
}
