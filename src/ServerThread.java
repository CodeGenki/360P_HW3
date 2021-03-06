import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ServerThread extends Thread{
    private Inventory inventory;
    private Socket client;

    public ServerThread(Inventory inventory, Socket client){
        this.inventory = inventory;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            Scanner sc = new Scanner(client.getInputStream());
            PrintWriter pout = new PrintWriter(client.getOutputStream());
            String command = sc.nextLine();
            String[] tokens = command.split(" ");

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
                            pout.println(message);
                            pout.flush();
                        } else{
                            // Tell client: 'Request Failed - Car not available'
                            String message = "Request Failed - Car not available";
                            pout.println(message);
                            pout.flush();
                        }
                    } else{
                        // Tell client: 'Request Failed - We do not have this car'
                        String message = "Request Failed - We do not have this car";
                        pout.println(message);
                        pout.flush();
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
                        pout.println(message);
                        pout.flush();
                    } else{
                        // Tell client: <Record-id> not found, no such rental record
                        String message = recordID + " not found, no such rental record";
                        pout.println(message);
                        pout.flush();
                    }
                    break;
                case "inventory":
                    String info = "";
                    for(int i = 0; i < inventory.size(); i++){
                        info += inventory.getCarInfo(i) + ",";  // Use comma as a delimiter
                    }
                    info += ".";
                    pout.println(info);
                    pout.flush();
                    break;
                case "list":
                    String name = tokens[1];
                    if(inventory.customerExists(name) && (inventory.getCustomers().get(name).size() == 0)) {
                        String list = inventory.generateList(name);
                        pout.println(list);
                        pout.flush();
                    } else{
                        String response = "No record found for " + name + ",."; // Delimiter for client
                        pout.println(response);
                        pout.flush();
                    }
                    break;
                case "exit":
                    inventory.dump();
                    break;
            }
            client.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
