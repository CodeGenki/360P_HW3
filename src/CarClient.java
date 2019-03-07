import java.net.*;
import java.util.Scanner;
import java.io.*;

public class CarClient {
    public static void main (String[] args) {
        String hostAddress;
        int tcpPort;
        int udpPort;
        int clientId;

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
            System.out.println("\t(1) <command-file>: file with commands to the server");
            System.out.println("\t(2) client id: an integer between 1..9");
            System.exit(-1);
        }

        boolean isTCP = false;
        boolean isUDP = true;

        String commandFile = args[0];
        clientId = Integer.parseInt(args[1]);
        hostAddress = "localhost";
        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port

        try {
            InetAddress ia = InetAddress.getByName(hostAddress);
            Scanner sc = new Scanner(new FileReader(commandFile));

            // Data structures for UDP
            DatagramSocket datasocket = new DatagramSocket();

            // Data structures for TCP
            Socket tcpsocket;    // Placeholder
            Scanner din;  // Placeholder
            PrintStream pout;    // Placeholder

            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                if (tokens[0].equals("setmode")) {
                    // set the mode of communication for sending commands to the server
                    if(tokens[1].equals("T")){  // TCP
                        isTCP = true;
                        isUDP = false;
                    } else {                    // UDP
                        isUDP = true;
                        isTCP = false;
                    }
                }
                else if (tokens[0].equals("rent")) {
                    // send appropriate command to the server and display the
                    // appropriate responses from the server
                    if(isUDP){
                        int length = cmd.length();
                        byte[] buffer = new byte[length];
                        buffer = cmd.getBytes();
                        sendUDP(datasocket, buffer ,ia, udpPort);
                        DatagramPacket rPacket = recvUDP(datasocket);
                        String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
                        System.out.println(retstring);
                    } else{
                        tcpsocket = new Socket(ia, tcpPort);
                        din = new Scanner(tcpsocket.getInputStream());
                        pout = new PrintStream(tcpsocket.getOutputStream());
                        pout.println(cmd);
                        pout.flush();
                        String retstring = din.nextLine();
                        System.out.println(retstring);
                        tcpsocket.close();
                    }
                } else if (tokens[0].equals("return")) {
                    // send appropriate command to the server and display the
                    // appropriate responses from the server
                    if(isUDP){
                        int length = cmd.length();
                        byte[] buffer = new byte[length];
                        buffer = cmd.getBytes();
                        sendUDP(datasocket, buffer ,ia, udpPort);
                        DatagramPacket rPacket = recvUDP(datasocket);
                        String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
                        System.out.println(retstring);
                    } else{
                        tcpsocket = new Socket(ia, tcpPort);
                        din = new Scanner(tcpsocket.getInputStream());
                        pout = new PrintStream(tcpsocket.getOutputStream());
                        pout.println(cmd);
                        pout.flush();
                        String retstring = din.nextLine();
                        System.out.println(retstring);
                        tcpsocket.close();
                    }
                } else if (tokens[0].equals("inventory")) {
                    // send appropriate command to the server and display the
                    // appropriate responses from the server
                    if(isUDP){
                        int length = cmd.length();
                        byte[] buffer = new byte[length];
                        buffer = cmd.getBytes();
                        sendUDP(datasocket, buffer ,ia, udpPort);
                        DatagramPacket rPacket = recvUDP(datasocket);
                        String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
                        String[] cars = retstring.split(",");
                        System.out.println("Inventory listing: ");  // TODO: Remove after debugging
                        int index = 0;
                        while(!cars[index].equals(".")) {
                            System.out.println(cars[index]);
                            index++;
                        }
                    } else{
                        tcpsocket = new Socket(ia, tcpPort);
                        din = new Scanner(tcpsocket.getInputStream());
                        pout = new PrintStream(tcpsocket.getOutputStream());
                        pout.println(cmd);
                        pout.flush();
                        String retstring = din.nextLine();
                        String[] cars = retstring.split(",");
                        System.out.println("Inventory listing: ");  // TODO: Remove after debugging
                        int index = 0;
                        while(!cars[index].equals(".")) {
                            System.out.println(cars[index]);
                            index++;
                        }
                        tcpsocket.close();
                    }
                } else if (tokens[0].equals("list")) {
                    // send appropriate command to the server and display the
                    // appropriate responses from the server
                    if(isUDP){
                        int length = cmd.length();
                        byte[] buffer = new byte[length];
                        buffer = cmd.getBytes();
                        sendUDP(datasocket, buffer ,ia, udpPort);
                        DatagramPacket rPacket = recvUDP(datasocket);
                        String retstring = new String(rPacket.getData(), 0, rPacket.getLength());
                        String[] records = retstring.split(",");
                        System.out.println(tokens[1] + "'s rental listing: ");  // TODO: Remove after debugging
                        int index = 0;
                        while(!records[index].equals(".")) {
                            System.out.println(records[index]);
                            index++;
                        }
                    } else{
                        tcpsocket = new Socket(ia, tcpPort);
                        din = new Scanner(tcpsocket.getInputStream());
                        pout = new PrintStream(tcpsocket.getOutputStream());
                        pout.println(cmd);
                        pout.flush();
                        String retstring = din.nextLine();
                        String[] records = retstring.split(",");
                        System.out.println(tokens[1] + "'s rental listing: ");  // TODO: Remove after debugging
                        int index = 0;
                        while(!records[index].equals(".")) {
                            System.out.println(records[index]);
                            index++;
                        }
                        tcpsocket.close();
                    }
                } else if (tokens[0].equals("exit")) {
                    // send appropriate command to the server
                    if(isUDP) {
                        int length = cmd.length();
                        byte[] buffer = new byte[length];
                        buffer = cmd.getBytes();
                        sendUDP(datasocket, buffer, ia, udpPort);
                    } else{
                        tcpsocket = new Socket(ia, tcpPort);
                        din = new Scanner(tcpsocket.getInputStream());
                        pout = new PrintStream(tcpsocket.getOutputStream());
                        pout.println(cmd);
                        pout.flush();
                        tcpsocket.close();
                    }
                } else {
                    System.out.println("ERROR: No such command");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private static void sendUDP(DatagramSocket datasocket, byte[] buffer, InetAddress ia, int udpPort) throws IOException{
        DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
        datasocket.send(sPacket);
    }
    private static DatagramPacket recvUDP(DatagramSocket datasocket) throws IOException{
        int len = 1024;
        byte[] rbuffer = new byte[len];

        DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length);
        datasocket.receive(rPacket);
        return rPacket;
    }
}