import java.net.*;
import java.io.*;
import java.util.*;

public class TCPListener implements Runnable{
    private int tcpPort;
    private Inventory inventory;

    public TCPListener(Inventory inventory, int tcpPort){
        this.tcpPort = tcpPort;
        this.inventory = inventory;
    }

    @Override
    public void run() {
        try {
            // Make a listener to listen for new connections on this port
            ServerSocket listener = new ServerSocket(tcpPort);
            Socket s;

            while((s = listener.accept()) != null){
                // Create a new thread to handle client's request
                Thread t = new ServerThread(inventory, s);
                t.run();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("TCP Server terminated.");
    }
}
