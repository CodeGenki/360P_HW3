import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

// Thread safe data structure for inventory
// Implement safety through monitors
class Inventory {
    private ArrayList<Car> inventory;
    private HashMap<String, ArrayList<Integer>> customers;
    private HashMap<Integer, Record> activeRecords;
    private int recordNumber;
    private String fileName;

    public Inventory(String fileName){
        inventory = new ArrayList<Car>();
        recordNumber = 0;
        customers = new HashMap<String, ArrayList<Integer>>();
        activeRecords = new HashMap<Integer, Record>();
        this.fileName = fileName;
    }

    /*
        Dump the inventory into the file
     */
    public synchronized void dump() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for(int i = 0; i < inventory.size(); i++){
            writer.write(getCarInfo(i));
            if(i < inventory.size() - 1)
                writer.write("\n");
        }
        writer.close();
    }
    /*
        Methods for inventory ArrayList
     */
    public void addCar(Car car){
        inventory.add(car);
    }
    public boolean contains(Car car){
        return inventory.contains(car);
    }
    public Car getCar(Car car){
        return inventory.get(inventory.indexOf(car));
    }
    public int size(){
        return inventory.size();
    }
    public String getCarInfo(int index){
        Car car = inventory.get(index);
        return car.getBrand() + " " + car.getColor() + " " + car.getCount();
    }

    /*
        Methods for book keeping structures
     */
    public HashMap<String, ArrayList<Integer>> getCustomers(){
        return this.customers;
    }
    public HashMap<Integer, Record> getActiveRecords(){
        return this.activeRecords;
    }
    public synchronized boolean recordExists(int id){
        return activeRecords.containsKey(id);
    }
    public synchronized void putRecord(int key, Record value){
        activeRecords.put(key, value);
    }
    public synchronized boolean customerExists(String name){
        return customers.containsKey(name);
    }
    public synchronized void putCustomer(String key, ArrayList<Integer> value){
        customers.put(key, value);
    }
    public synchronized void customerAddCar(String name, int id){
        customers.get(name).add(id);
    }
    public synchronized  void customerRemoveCar(String name, int id){
        customers.get(name).remove((Integer)id);
    }
    public synchronized Car recordGetCar(int id){
        return activeRecords.get(id).getCar();
    }
    public String recordGetCustomerName(int id){
        return activeRecords.get(id).getCustomerName();
    }
    public synchronized void removeRecord(int id){
        activeRecords.remove(id);
    }
    public synchronized String generateList(String name){
        String list = "";
        ArrayList<Integer> records = customers.get(name);
        for(int record : records){
            list += record + " " +
                    activeRecords.get(record).getCarInfo() + ",";   // Use comma as delimiter
        }
        list += ".";
        return list;
    }
    /*
        Issue a unique record number
     */
    public synchronized int issueRecordNumber(){
        recordNumber++;
        return recordNumber;
    }


}