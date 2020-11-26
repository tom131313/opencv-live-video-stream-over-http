package editImage;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Size;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ImageEdit {

    private static Mat tempImage = new Mat();

public static Mat edit(Mat src) {

    // returning temp assumes output Mat is different memory than input
    // this example case that is necessary but do it anyway for example

    tempImage = src.clone();

    //Mat.zeros(frame.rows(), frame.cols(), frame.type()).copyTo(frame); // wipe out camera image with black (zeros)

    // - Add a circle
    Point   center = new Point(50, 50);
    int     radius = 40;
    Scalar  color = new Scalar(0, 255, 0);
    int     thickness = Imgproc.FILLED;

    Imgproc.circle( tempImage, center, radius, color, thickness );

    // - Add a rectangle
    MatOfPoint rectangle = new MatOfPoint(
        new Point(130., 30.),
        new Point(180., 30.),
        new Point(180., 180.),
        new Point(130., 180.)
        );

    drawShape( rectangle, tempImage );

    Imgproc.resize(tempImage, tempImage, new Size(120., 100.));
    return tempImage;
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
