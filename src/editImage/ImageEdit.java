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

    // Returning temp assumes output Mat is different memory address than input
    // since Java doesn't pass by reference.  Whether or not that is needed depends
    // on what are the changes to the data.  This is the general case that supports
    // whatever might happen.
 
    tempImage = src.clone();

    //Mat.zeros(frame.rows(), frame.cols(), frame.type()).copyTo(frame); // wipe out camera image with black (zeros)

    // - Add a circle
    Point   center = new Point(50, 50);
    int     radius = 40;
    Scalar  color = new Scalar(0, 255, 0);
    int     thickness = Imgproc.FILLED;

    Imgproc.circle( tempImage, center, radius, color, thickness );

    // - Add a rectangle and some other polygon
    List<MatOfPoint> polygon = new ArrayList<MatOfPoint>(2);
    polygon.add( new MatOfPoint (
        new Point(130., 30.),
        new Point(180., 30.),
        new Point(180., 180.),
        new Point(130., 180.) ) );

    polygon.add( new MatOfPoint (
        new Point(140., 40.),
        new Point(190., 40.),
        new Point(200., 200.),
        new Point(60., 180.) ) );

    drawShape( polygon, tempImage );

    Imgproc.resize(tempImage, tempImage, new Size(120., 100.));
    
    return tempImage;
}

static void drawShape(List<MatOfPoint> shape, Mat dst) {

    // draw an auto-closed polygon - first and last point don't have to match
    Imgproc.polylines(
        dst, // Matrix obj of the image
        shape, // java.util.List<MatOfPoint> pts
        true, // isClosed
        new Scalar(0, 255, 0), // Scalar object for color
        1, // Thickness of the line
        Imgproc.LINE_4 // line type
        );

    // draw markers on the first curve .get(0)
    for (int idxR =0; idxR < shape.get(0).rows(); idxR++) {
        int c =  (int)shape.get(0).get(idxR,0) [0];
        int r =  (int)shape.get(0).get(idxR,0) [1];
        Imgproc.drawMarker(dst, new Point(c, r), new Scalar(255, 0, 255), Imgproc.MARKER_STAR, 8);
        }
    }
}
