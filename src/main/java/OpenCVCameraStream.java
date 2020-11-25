package app;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class OpenCVCameraStream extends Thread {

    static VideoCapture videoCapture;
    protected static Mat image = new Mat(); // shared; lock ReadWrite
    private static Mat frame = new Mat(); // internal; sync copy to image when completed
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

            // > Code to generate test images
            generateTestImage(); // mess with the frame before serving it
            // > END Code to generate test images

            //procesed frame comlete; copy to synced image for others to view
            Server.lockImage.writeLock().lock();
            image = frame;
            frameCount++;
            Server.lockImage.writeLock().unlock();
            synchronized(aLock){aLock.notifyAll();}
        }
    }

//////////////////////////////////////////////////////////////////////
//
// GENERATE TEST IMAGE FROM CAMERA FRAME
//
//////////////////////////////////////////////////////////////////////

public static void generateTestImage() {

    //Mat.zeros(frame.rows(), frame.cols(), frame.type()).copyTo(frame); // wipe out camera image with black (zeros)

    // - Add a circle
    Point   center = new Point(50, 50);
    int     radius = 40;
    Scalar  color = new Scalar(0, 255, 0);
    int     thickness = Imgproc.FILLED;

    Imgproc.circle( frame, center, radius, color, thickness );

    // - Add a rectangle
    MatOfPoint rectangle = new MatOfPoint(
        new Point(130., 30.),
        new Point(180., 30.),
        new Point(180., 180.),
        new Point(130., 180.)
        );

    drawShape( rectangle, frame );
}

static void drawShape(MatOfPoint shape, Mat dst) {
    //System.out.println(shape.dump() + " " + dst.rows()); 

    List<MatOfPoint> temp = new ArrayList<MatOfPoint>();
    temp.add(shape);
    Imgproc.polylines(
        dst, // Matrix obj of the image
        temp, // java.util.List<MatOfPoint> pts
        true, // isClosed
        new Scalar(0, 255, 0), // Scalar object for color
        1, // Thickness of the line
        Imgproc.LINE_4 // line type
        );

    // for (int idxR =0; idxR < shape.rows(); idxR++) {
    //     int c =  (int)shape.get( idxR,0) [0];
    //     int r =  (int)shape.get( idxR,0) [1];
    //     Imgproc.drawMarker(dst, new Point(c, r), new Scalar(255, 0, 255), Imgproc.MARKER_STAR, 8);// magenta
    // }
    }


}
