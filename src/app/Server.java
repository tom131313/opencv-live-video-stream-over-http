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

    protected static int quality = 100; // default quality
    protected static Mat frame = null;
    protected static HttpStreamServer httpStreamService;
    protected static ReadWriteLock lockImage = new ReentrantReadWriteLock(true);

    public static void main(String[] args) {

        // start the camera capture/draw an image thread
        OpenCVCameraStream ci = new OpenCVCameraStream();
        Thread imageThread = new Thread(ci);
        imageThread.start();

        // get the port number and JPG quality options
        int port = 8080; // default port for example, http://localhost:8080
        try {
            if(args.length > 0) {
                port = Integer.parseInt(args[0]);
                if(port < 0 ) {
                    throw new NumberFormatException();
                    }
                }
            if(args.length > 1) {
                quality = Integer.parseInt(args[1]);
                if(quality < 0 || quality > 100) {
                    throw new NumberFormatException();
                    }
                }
        }
        catch(NumberFormatException ex) {
            System.out.println("Usage:java -jar OpenCVServer.jar <port number> <JPG stream quality 0-100>");
            System.out.println("Default 8080 100");
            System.out.flush();
            System.exit(1);
            }

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
