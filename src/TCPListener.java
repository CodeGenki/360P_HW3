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
            System.out.println("TCP Server is running. Listening for connections...");// TODO: Remove after debugging
            ServerSocket listener = new ServerSocket(tcpPort);
            Socket s;

            while((s = listener.accept()) != null){
                // Create a new thread to handle client's request
                System.out.println("Accepted TCP connection.");     // TODO: Remove after debugging
                Thread t = new ServerThread(inventory, s);
                t.run();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("TCP Server terminated");
    }
}
