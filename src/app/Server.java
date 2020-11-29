package app;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {

    static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);} // Load opencv native library

    protected static int camera = 0; // default camera
    protected static int port = 8080; // default port for example, http://localhost:8080
    protected static int quality = 100; // default quality, best quality-lowest compression

    protected static Mat frame = null;
    protected static HttpStreamServer httpStreamService;
    protected static ReadWriteLock lockImage = new ReentrantReadWriteLock(true);

    public static void main(String[] args) {

        // get the camera number, port number and JPG quality options
        try {
           if(args.length > 0) {
                camera = Integer.parseInt(args[0]);
                if(camera < 0) {
                    throw new NumberFormatException();
                    }
                }
            if(args.length > 1) {
                port = Integer.parseInt(args[1]);
                if(port < 0 ) {
                    throw new NumberFormatException();
                    }
                }
            if(args.length > 2) {
                quality = Integer.parseInt(args[2]);
                if(quality < 0 || quality > 100) {
                    throw new NumberFormatException();
                    }
                }
             }
        catch(NumberFormatException ex) {
            System.out.println("Usage:java -jar OpenCVServer.jar <camera number> <port number> <JPG stream quality 0-100>");
            System.out.println("Default 0 8080 100");
            System.out.flush();
            System.exit(1);
            }
        
        System.out.println(camera + " " + port + " " + quality);

       // start the camera capture/draw an image thread
        OpenCVCameraStream ci = new OpenCVCameraStream();
        Thread imageThread = new Thread(ci);
        imageThread.start();

        // establish server socket to desired port
        // loop forever waiting for clients and then serving each client request
        try (
            ServerSocket serverSocket = new ServerSocket(port);
            ) {
 
        while (true) {
            System.out.println("Waiting for client request on http://localhost:" + port);
            Socket socket = serverSocket.accept(); // accept connection with the assigned socket
            System.out.println("New client asked for a connection " + socket.getPort());
            httpStreamService = new HttpStreamServer(socket); // define the server to the socket
            Thread t = new Thread(httpStreamService); // define the thread
            t.start(); // start the thread
         }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
