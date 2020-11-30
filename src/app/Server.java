package app;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    } // Load opencv native library

    protected static Thread mainThread = Thread.currentThread();

    protected static int camera = 0; // default camera
    protected static int port = 8080; // default port for example, http://localhost:8080
    protected static int quality = 100; // default quality, best quality-lowest compression

    protected static Mat frame = null;
    protected static HttpStreamServer httpStreamService;
    protected static ReadWriteLock lockImage = new ReentrantReadWriteLock(true);

    public static void main(String[] args) {

        mainThread.setName("ServerMain");

        // get the camera number, port number and JPG quality options
        try {
            if (args.length > 0) {
                camera = Integer.parseInt(args[0]);
                if (camera < 0) {
                    throw new NumberFormatException();
                }
            }
            if (args.length > 1) {
                port = Integer.parseInt(args[1]);
                if (port < 0) {
                    throw new NumberFormatException();
                }
            }
            if (args.length > 2) {
                quality = Integer.parseInt(args[2]);
                if (quality < 0 || quality > 100) {
                    throw new NumberFormatException();
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(
                "Usage:java -jar OpenCVServer.jar <camera number> <port number> <JPG stream quality 0-100>");
            System.out.println("Default options: 0 8080 100");
            System.exit(1);
        }

        System.out.println("Camera " + camera + ", Port " + port + ", JPG quality " + quality);

        // start the camera capture/draw an image thread
        OpenCVCameraStream ci = new OpenCVCameraStream();
        Thread cameraThread = new Thread(ci);
        cameraThread.setName("Camera");
        System.out.println("Starting " + cameraThread);
        cameraThread.start();

        // establish server socket to desired port
        // loop forever waiting for clients and then serving each client request
        // check every Socket Timeout period to see if main was interrupted by others and should quit
        try (
            ServerSocket serverSocket = new ServerSocket(port);
            ) {
 
        serverSocket.setSoTimeout(5000); // short timeout delay for checking if a thread interrupted main
        Socket socket;
        Thread httpThread = null;

        clientLoop:
        while (!mainThread.isInterrupted()) {
            System.out.println("Waiting for another client request on http://localhost:" + port);
            
            while(true) { // make the accept interruptable to get messages from others
                try {
                socket = serverSocket.accept(); // wait and accept connection with the assigned socket
                break; // made the connection so stop waiting and continue on to start the thread
                // check for request to interrupt in the last timeout period and if so bailout
                } catch(SocketTimeoutException e) {if(mainThread.isInterrupted()) break clientLoop;}
            }

            System.out.println("New client asked for a connection " + socket.getPort());
            httpStreamService = new HttpStreamServer(socket); // define the server to the socket
            httpThread = new Thread(httpStreamService); // define the thread
            httpThread.setName("ServerHTTP");
            System.out.println("Starting " + httpThread);
            httpThread.start(); // start the thread
        }

        System.out.println("Server main done; trying to cleanup");
        // suggest killing the others
        if(cameraThread != null) cameraThread.interrupt();
        if(httpThread != null) httpThread.interrupt();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
