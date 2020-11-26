package app;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

public class HttpStreamServer implements Runnable {

    private Socket socket;
    private OutputStream outputStream;
    private final String boundary = "stream";
    private Mat image =  new Mat();

    public HttpStreamServer(Socket socket) {
        this.socket = socket;
        System.out.println("Server constructed using " + this.socket);
    }

    public void run() {
        try {
            outputStream = this.socket.getOutputStream();
            writeHeader(); // header needed at the start of the stream

            // loop forever reading and serving images
            while (true) {
                synchronized(OpenCVCameraStream.aLock){OpenCVCameraStream.aLock.wait();}
                Server.lockImage.readLock().lock();
                if(OpenCVCameraStream.image != null) image = OpenCVCameraStream.image.clone(); // get the current image
                Server.lockImage.readLock().unlock();
                pushImage(image, Server.quality); // send OpenCV image to the socket as a compressed JPG bytes
            }
        }
        catch (IOException | InterruptedException e) {return;}
        finally{System.out.println("thread terminated");}
    }

    private void writeHeader() throws IOException, SocketException{
        outputStream.write(("HTTP/1.0 200 OK\r\n" +
                "Connection: close\r\n" +
                "Max-Age: 0\r\n" +
                "Expires: 0\r\n" +
                "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
                "Pragma: no-cache\r\n" +
                "Content-Type: multipart/x-mixed-replace; " +
                "boundary=" + boundary + "\r\n" +
                "\r\n" +
                "--" + boundary + "\r\n").getBytes());
    }

    public void pushImage(Mat image, int quality) throws IOException {
        if (image == null || image.empty())
            return;

        byte[] imageBytes = Mat2byteArray(image, quality);

        outputStream.write(("Content-type: image/jpeg\r\n" +
                "Content-Length: " + imageBytes.length + "\r\n" +
                "\r\n").getBytes());

        outputStream.write(imageBytes);

        outputStream.write(("\r\n--" + boundary + "\r\n").getBytes());
    }

   public static byte[] Mat2byteArray(Mat image, int quality) throws IOException {

        // jpg quality; 0 high compression to 100 low compression

        MatOfByte bytemat = new MatOfByte();

        MatOfInt parameters = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality); // pair-wise; param1, value1, ...

        Imgcodecs.imencode(".jpg", image, bytemat, parameters);

        return bytemat.toArray();
    }
    
    public void stopStreamingServer() throws IOException {
        socket.close();
     }
 
}