package app;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class OpenCVCameraStream extends Thread {

    private static VideoCapture videoCapture;
    protected static Mat image = new Mat(); // shared; lock ReadWrite
    private static Mat frame = new Mat(); // internal; sync copy to image when completed
    private static Mat tempImage = new Mat();
    protected static int frameCount = 0;
    protected static Object aLock = new Object();

    public void run() {

        videoCapture = new VideoCapture();
        videoCapture.open(0); // camera number
        if (!videoCapture.isOpened()) {
            return;
        }
        while(true) {

            if (!videoCapture.read(frame)) {
                break;
                }


            tempImage = ImageEdit.edit(frame); // mess with the frame before serving it


            //processed frame comlete; copy to synced image for others to view
            Server.lockImage.writeLock().lock();
            image = tempImage.clone();
            frameCount++;
            Server.lockImage.writeLock().unlock();
            synchronized(aLock){aLock.notifyAll();}
        }
    }
}
