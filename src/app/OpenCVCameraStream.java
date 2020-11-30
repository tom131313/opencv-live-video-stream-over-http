package app;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import editImage.ImageEdit;

public class OpenCVCameraStream extends Thread {
 
    public class LastChanceHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Server.mainThread.interrupt(); // bad to be here so tell the main it should quit
            System.out.println("Last chance in thread " + t);
            e.printStackTrace();
        }
    }

    private static VideoCapture videoCapture;
    protected static Mat image = new Mat(); // shared; lock ReadWrite
    private static Mat frame = new Mat(); // internal; sync copy to image when completed
    private static Mat tempImage = new Mat();
    protected static int frameCount = 0;
    protected static Object aLock = new Object();
    protected static boolean editImage = true; // assume image editing class was loaded

    public void run() {

        Thread.setDefaultUncaughtExceptionHandler(new LastChanceHandler());

        videoCapture = new VideoCapture();
        
        if (videoCapture.open(Server.camera)) { // open requested camera
            // loop grabing camera frames until requested to stop
            while(!Thread.currentThread().isInterrupted()) {

                if (!videoCapture.read(frame)) {
                    break;
                    }

                try {

                // call any frame editing that someone may have put into the classpath
                if(editImage) {
                    try {
                        tempImage = ImageEdit.edit(frame); // edit the frame before serving it
                    } catch (NoClassDefFoundError | NoSuchMethodError e) {
                        editImage = false;
                        System.out.println("Mat ImageEdit.edit(Mat) not found in classpath; no image editing");
                    }
                }
                else {
                    tempImage = frame.clone(); // frame used unedited
                }

                } catch(Exception e) { // catch whatever bad may be returned
                    e.printStackTrace();
                    break;
                }

                //processed frame complete; copy to synced image for others to view
                Server.lockImage.writeLock().lock();
                image = tempImage.clone(); // get the new image to distribute
                frameCount++;
                Server.lockImage.writeLock().unlock();
                synchronized(aLock) {
                    aLock.notifyAll(); // tell all there is a new image
                }
            }
        }
        else {
            System.out.println("Unable to open camera " + Server.camera);
        }

        // should have run forever but failed or interrupted so quit
        Server.mainThread.interrupt(); // tell main there was a problem and it should quit
        System.exit(1); // quit
    }
}
