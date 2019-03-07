import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class CarServer {
    public static void main (String[] args) {
        int tcpPort;
        int udpPort;
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;
        int len = 1024;

        Inventory inventory = new Inventory(fileName);

        // parse the inventory file
        try {
            Scanner sc = new Scanner(new FileReader(fileName));
            while(sc.hasNextLine()) {
                String carData = sc.nextLine();
                String[] tokens = carData.split(" ");

                String brand = tokens[0];
                String color = tokens[1];
                int count = Integer.parseInt(tokens[2]);

                Car newCar = new Car(brand, color, count);
                inventory.addCar(newCar);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Start a new thread that accepts TCP requests from clients
        Thread tcpListener = new Thread(new TCPListener(inventory, tcpPort));
        tcpListener.start();

        // Make main thread accept UDP requests. TCP will listen inside another thread.
        DatagramPacket datapacket, returnpacket;
        try {
            DatagramSocket datasocket = new DatagramSocket(udpPort);
            byte[] buf = new byte[len];
            while(true){
                datapacket = new DatagramPacket(buf, buf.length);
                datasocket.receive(datapacket);

                String cmd = new String(datapacket.getData(), 0, datapacket.getLength());
                String[] tokens = cmd.split(" ");

                switch(tokens[0]){
                    case "rent":
                        String customerName = tokens[1];
                        String carBrand = tokens[2];
                        String color = tokens[3];

                        // Check the inventory.
                        Car wantedCar = new Car(carBrand, color, 1);
                        if(inventory.contains(wantedCar)){
                            if(inventory.getCar(wantedCar).getCount() > 0){
                                // Tell client: 'Your request has been approved, <id> <name> <brand> <color>
                                int recordID = inventory.issueRecordNumber();
                                inventory.putRecord(recordID, new Record(customerName, wantedCar));
                                if(!inventory.customerExists(customerName))
                                    inventory.putCustomer(customerName, new ArrayList<Integer>());
                                inventory.customerAddCar(customerName, recordID);

                                inventory.getCar(wantedCar).decrementCount();
                                String message = "Your request has been approved, " + recordID
                                        + " " + customerName
                                        + " " + carBrand
                                        + " " + color;
                                udpSendMessage(message, datapacket.getAddress(), datapacket.getPort(), datasocket);
                            } else{
                                // Tell client: 'Request Failed - Car not available'
                                String message = "Request Failed - Car not available";
                                udpSendMessage(message, datapacket.getAddress(), datapacket.getPort(), datasocket);
                            }
                        } else{
                            // Tell client: 'Request Failed - We do not have this car'
                            String message = "Request Failed - We do not have this car";
                            udpSendMessage(message, datapacket.getAddress(), datapacket.getPort(), datasocket);
                        }
                        break;
                    case "return":
                        int recordID = Integer.parseInt(tokens[1]);
                        if(inventory.recordExists(recordID)){
                            inventory.getCar(inventory.recordGetCar(recordID)).incrementCount();
                            inventory.customerRemoveCar(inventory.recordGetCustomerName(recordID), recordID);
                            inventory.removeRecord(recordID);
                            // Tell client: <record-id> is returned
                            String message = recordID + " is returned";
                            udpSendMessage(message, datapacket.getAddress(), datapacket.getPort(), datasocket);
                        } else{
                            // Tell client: <Record-id> not found, no such rental record
                            String message = recordID + " not found, no such rental record";
                            udpSendMessage(message, datapacket.getAddress(), datapacket.getPort(), datasocket);
                        }
                        break;
                    case "inventory":
                        String info = "";
                        for(int i = 0; i < inventory.size(); i++){
                            info += inventory.getCarInfo(i) + ",";  // Use comma as a delimiter
                        }
                        info += ".";
                        udpSendMessage(info, datapacket.getAddress(), datapacket.getPort(), datasocket);
                        break;
                    case "list":
                        String name = tokens[1];
                        String list = "";
                        ArrayList<Integer> records = inventory.getCustomers().get(name);
                        for(int record : records){
                            list += record + " " +
                                    inventory.getActiveRecords().get(record).getCarInfo() + ",";   // Use comma as delimiter
                        }
                        list += ".";
                        udpSendMessage(list, datapacket.getAddress(), datapacket.getPort(), datasocket);
                        break;
                    case "exit":
                        // print inventory to "inventory.txt"
                        inventory.dump();
                        break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void udpSendMessage(String message, InetAddress ia, int udpPort,
                                       DatagramSocket datasocket) throws IOException{
        byte[] sBuffer = new byte[message.length()];
        sBuffer = message.getBytes();
        DatagramPacket sPacket = new DatagramPacket(sBuffer, sBuffer.length, ia, udpPort);
        datasocket.send(sPacket);
    }
}

class Record {
    private String customerName;
    private Car car;
    public Record(String customerName, Car car){
        this.customerName = customerName;
        this.car = car;
    }

    public String getCustomerName(){
        return this.customerName;
    }
    public Car getCar(){
        return this.car;
    }
    public String getCarInfo(){
        return this.car.getBrand() + " " + this.car.getColor();
    }
}

// Monitors are per-object, so this allows other threads to
// access other Car objects in the inventory.
class Car {
    private String brand;
    private String color;
    private int count;

    public Car(String brand, String color, int count){
        this.brand = brand;
        this.color = color;
        this.count = count;
    }

    public String getBrand(){
        return this.brand;
    }
    public String getColor(){
        return this.color;
    }

    // These methods can only be executed by one thread at a time
    // for this specific object.
    // No other thread can access any of these methods while in use.
    public synchronized int getCount(){
        return this.count;
    }
    public synchronized void decrementCount(){
        this.count--;
    }
    public synchronized  void incrementCount(){
        this.count++;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass().equals(this.getClass()))      // Check if compared object is same type
            return(((Car)obj).brand.equals(this.brand) && ((Car)obj).color.equals(this.color));
        else
            return false;
    }
}