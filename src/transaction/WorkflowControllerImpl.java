package transaction;

import lockmgr.DeadlockException;
import transaction.exception.InvalidIndexException;
import transaction.exception.InvalidTransactionException;
import transaction.exception.TransactionAbortedException;
import transaction.model.*;

import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.Collection;
import java.util.HashSet;

/**
 * Workflow Controller for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the WC.  In the real
 * implementation, the WC should forward calls to either RM or TM,
 * instead of doing the things itself.
 */

public class WorkflowControllerImpl
        extends java.rmi.server.UnicastRemoteObject
        implements WorkflowController {

    protected int flightCounter, flightPrice, carsCounter, carsPrice, roomsCounter, roomsPrice;
    protected int xidCounter;

    protected ResourceManager rmFlights = null;
    protected ResourceManager rmRooms = null;
    protected ResourceManager rmCars = null;
    protected ResourceManager rmCustomers = null;
    protected TransactionManager tm = null;

    private HashSet<Integer> xids = new HashSet<>();

    private String WC_XID_FILEPATH = "WC_xids.log";

    public static void main(String args[]) {
        System.setSecurityManager(new SecurityManager());

        String rmiPort = System.getProperty("rmiPort");
        rmiPort = Utils.genrConSyntax(rmiPort);

        try {
            WorkflowControllerImpl obj = new WorkflowControllerImpl();
            Naming.rebind(rmiPort + WorkflowController.RMIName, obj);
            System.out.println("WC bound");
        } catch (Exception e) {
            System.err.println("WC not bound:" + e);
            System.exit(1);
        }
    }


    public WorkflowControllerImpl() throws RemoteException {

        this.xids = new HashSet<>();

        // recover from the disk memory, recover the unfinished xids
        this.recover();

        while (!reconnect()) {
            // would be better to sleep a while
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void recover(){
        // read the logs in disk memory
        File xidLog = new File(WC_XID_FILEPATH);

        HashSet<Integer>  hashSet  = null;

        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(new FileInputStream(xidLog));
            hashSet = (HashSet<Integer>)ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ois != null)
                    ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (hashSet != null) {
            this.xids = hashSet;
        }
    }


    private void updateXIDFile(){
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(WC_XID_FILEPATH));
            oos.writeObject(this.xids);
        } catch(Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(oos != null) {
                    oos.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }


    // TRANSACTION INTERFACE
    public int start()
            throws RemoteException {
        int xid = tm.start();

        this.xids.add(xid);

        //store xid in disk
        this.updateXIDFile();


        return xid;
    }

    public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        System.out.println("Committing");

        // if there doesn't exist this transaction, throw Invalid Transaction Exception
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "commit");

        // call the commit method of TM
        boolean commitSuccess = tm.commit(xid);

        // remove the xid from ongoing xid set
        xids.remove(xid);
        this.updateXIDFile();

        return commitSuccess;
    }

    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException {

        //if there doesn't exist this transaction, throw Invalid Transaction Exception
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "abort");
        tm.abort(xid);
        xids.remove(xid);
        this.updateXIDFile();
    }


    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // if there doesn't exist this transaction, throw Invalid Transaction Exception
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "addFlight");
        }

        // check the flightNum and the numSeats valid
        if (flightNum == null || numSeats < 0) {
            return false;
        }

//        price = price < 0 ? 0 : price;

        // find if there already exists flightNum
        ResourceItem resourceItem;
        try {
            resourceItem = this.rmFlights.query(xid, this.rmFlights.getID(), flightNum);
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }

        // new flight, call RM to insert new flight
        if (resourceItem == null) {
            if(price < 0){
                price = 0;
            }

            Flight flight = new Flight(flightNum, price, numSeats, numSeats);

            try {
                return this.rmFlights.insert(xid, this.rmFlights.getID(), flight);
            } catch (DeadlockException e) {
                this.abort(xid);
                throw new TransactionAbortedException(xid, e.getMessage());
            }
        }

        // existing flight, add the seats and overwrite price
        Flight flight = (Flight) resourceItem;
        flight.addSeats(numSeats);

        if (price >= 0){
            flight.setPrice(price);
        }

        // update
        try {
            return this.rmFlights.update(xid, this.rmFlights.getID(), flightNum, flight);
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }

    }

    public boolean deleteFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        //check if the transaction exists
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "deleteFlight");
        }

        //check if the flightNum is valid
        if (flightNum == null) {
            return false;
        }

        ResourceItem resourceItem = null;
        try {
            resourceItem = this.rmFlights.query(xid, this.rmFlights.getID(), flightNum);
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }

        // the flight doesn't exist
        if (resourceItem == null) {
            return false;
        }

        Collection reservations = null;
        try {
            // find all the reservations related to the deleted flight
            reservations = this.rmCustomers.query(xid, ResourceManager.TableNameReservations, Reservation.INDEX_CUSTNAME, flightNum);
            if (!reservations.isEmpty()) {
                return false;
            }

            return this.rmFlights.delete(xid, this.rmFlights.getID(), flightNum);
        }catch(DeadlockException e){
            this.abort(xid);
            throw new TransactionAbortedException(xid,e.getMessage());
        }catch(InvalidIndexException iie){
            iie.printStackTrace();
        }

        return false;
    }

    public boolean addRooms(int xid, String location, int numRooms, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if transaction already exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "addRooms");
        }

        // check if the location and numRooms valid
        if (location == null || numRooms < 0) {
            return false;
        }

        // check if the hotel already exist at the location
        ResourceItem resourceItem;
        try {
            resourceItem = this.rmRooms.query(xid, this.rmRooms.getID(), location);

            // new hotel
            if (resourceItem == null) {
                Hotel hotel = new Hotel(location, price, numRooms, numRooms);
                return this.rmRooms.insert(xid, this.rmRooms.getID(), hotel);
            }else {

                // existing hotel
                Hotel hotel = (Hotel) resourceItem;
                hotel.addRooms(numRooms);

                if (price >= 0) {
                    hotel.setPrice(price);
                }

                return this.rmRooms.update(xid, this.rmRooms.getID(), location, hotel);
            }


        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }


    public boolean deleteRooms(int xid, String location, int numRooms)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "deleteRooms");
        }

        // check if the input is valid
        if (location == null || numRooms < 0) {
            return false;
        }

        try {
            ResourceItem resourceItem = this.rmRooms.query(xid, this.rmRooms.getID(), location);
            // the hotel doesn't exist
            if (resourceItem == null) {
                return false;
            }else {
                Hotel hotel = (Hotel) resourceItem;
                if(!hotel.reduceRooms(numRooms)){
                    return false;
                }

                return this.rmRooms.update(xid, this.rmRooms.getID(), location, hotel);
            }
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public boolean addCars(int xid, String location, int numCars, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction exists
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "addCars");
        }

        // check if the input is valid
        if (location == null || numCars < 0) {
            return false;
        }


        try {
            ResourceItem resourceItem = this.rmCars.query(xid, this.rmCars.getID(), location);

            if (resourceItem == null) {
                Car car = new Car(location, price, numCars, numCars);

                return this.rmCars.insert(xid, this.rmCars.getID(), car);
            }else{
                Car car = (Car) resourceItem;
                car.addCars(numCars);

                if(price >= 0) {
                    car.setPrice(price);
                }

                return this.rmCars.update(xid, this.rmCars.getID(), location, car);

            }


        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }


    }

    public boolean deleteCars(int xid, String location, int numCars)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "deleteCars");
        }

        // check if the input is valid
        if (location == null || numCars < 0) {
            return false;
        }

        try {
            ResourceItem resourceItem = this.rmCars.query(xid, this.rmCars.getID(), location);

            if (resourceItem == null) {
                return false;
            }

            Car car = (Car) resourceItem;
            if (!car.reduceCars(numCars)) {
                return false;
            }

            return this.rmCars.update(xid, this.rmCars.getID(), location, car);
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public boolean newCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction already exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "newCustomer");
        }

        //check if the input is valid
        if (custName == null) {
            return false;
        }

        try {
            ResourceItem resourceItem = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);

            if (resourceItem != null) {
                return true;
            }else{
                Customer customer = new Customer(custName);
                return this.rmCustomers.insert(xid, this.rmCustomers.getID(), customer);
            }


        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public boolean deleteCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "deleteCustomer");
        }

        // check if the input is valid
        if (custName == null) {
            return false;
        }

        try {
            ResourceItem resourceItem = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);
            if (resourceItem == null) {
                return false;
            }

            // find and cancel all reservation related to the customer
            Collection reservations = this.rmCustomers.query(xid, ResourceManager.TableNameReservations, Reservation.INDEX_CUSTNAME, custName);

            for (Object obj : reservations) {
                Reservation reservation = (Reservation) obj;
                String resKey = reservation.getResvKey();
                int resType = reservation.getResvType();

                if (resType == Reservation.RESERVATION_TYPE_FLIGHT) {
                    Flight flight = (Flight) this.rmFlights.query(xid, this.rmFlights.getID(), resKey);
                    flight.cancelResv(1);
                    this.rmFlights.update(xid, this.rmFlights.getID(), resKey, flight);
                } else if (resType == Reservation.RESERVATION_TYPE_HOTEL) {
                    Hotel hotel = (Hotel) this.rmRooms.query(xid, this.rmRooms.getID(), resKey);
                    hotel.cancelResv(1);
                    this.rmRooms.update(xid, this.rmRooms.getID(), resKey, hotel);
                } else if (resType == Reservation.RESERVATION_TYPE_CAR) {
                    Car car = (Car) this.rmCars.query(xid, this.rmCars.getID(), resKey);
                    car.cancelResv(1);
                    this.rmCars.update(xid, this.rmCars.getID(), resKey, car);
                }
            }
            //delete the record in the reservation tables
            this.rmCustomers.delete(xid, ResourceManager.TableNameReservations, Reservation.INDEX_CUSTNAME, custName);

            return this.rmCustomers.delete(xid, this.rmCustomers.getID(), custName);
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        } catch (InvalidIndexException e) {
            e.printStackTrace();
        }

        return false;
    }


    // QUERY INTERFACE
    public int queryFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "queryFlight");
        }

        // check the input valid
        if (flightNum == null) {
            return -1;
        }

        try {
            ResourceItem resourceItem = this.rmFlights.query(xid, this.rmFlights.getID(), flightNum);
            if (resourceItem == null) {
                return -1;
            }

            return ((Flight) resourceItem).getNumAvail();
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryFlightPrice(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "queryFlightPrice");
        }

        // check the input valid
        if (flightNum == null) {
            return -1;
        }

        try {
            ResourceItem resourceItem = this.rmFlights.query(xid, this.rmFlights.getID(), flightNum);
            if (resourceItem == null) {
                return -1;
            }

            return ((Flight) resourceItem).getPrice();
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryRooms(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "queryRooms");
        }

        // check the input valid
        if (location == null) {
            return -1;
        }

        try {
            ResourceItem resourceItem = this.rmRooms.query(xid, this.rmRooms.getID(), location);
            if (resourceItem == null) {
                return -1;
            }

            return ((Hotel) resourceItem).getNumAvail();
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryRoomsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "queryRoomsPrice");
        }

        // check the input valid
        if (location == null) {
            return -1;
        }

        try {
            ResourceItem resourceItem = this.rmRooms.query(xid, this.rmRooms.getID(), location);
            if (resourceItem == null) {
                return -1;
            }

            return ((Hotel) resourceItem).getPrice();
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryCars(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "queryCars");
        }

        // check the input valid
        if (location == null) {
            return -1;
        }

        try {
            ResourceItem resourceItem = this.rmCars.query(xid, this.rmCars.getID(), location);
            if (resourceItem == null) {
                return -1;
            }

            return ((Car) resourceItem).getNumAvail();
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryCarsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "queryCarsPrice");
        }

        // check the input valid
        if (location == null) {
            return -1;
        }

        try {
            ResourceItem resourceItem = this.rmCars.query(xid, this.rmCars.getID(), location);
            if (resourceItem == null) {
                return -1;
            }

            return ((Car) resourceItem).getPrice();
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryCustomerBill(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "queryCarsPrice");
        }

        // check the input valid
        if (custName == null) {
            return -1;
        }
        try {
            ResourceItem resourceItem = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);
            if (resourceItem == null)
                return -1;

            Collection<ResourceItem> results = null;
            results = rmCustomers.query(xid, ResourceManager.TableNameReservations, Reservation.INDEX_CUSTNAME, custName);

            // no reservations
            if (results == null)
                return 0;

            int totalBill = 0;
            for (ResourceItem re : results) {
                Reservation resv = (Reservation) re;
                String resvKey = resv.getResvKey();
                int resvType = resv.getResvType();

                if (resvType == Reservation.RESERVATION_TYPE_FLIGHT) {
                    Flight flight = (Flight) this.rmFlights.query(xid, this.rmFlights.getID(), resvKey);
                    totalBill += flight.getPrice();
                } else if (resvType == Reservation.RESERVATION_TYPE_HOTEL) {
                    Hotel hotel = (Hotel) this.rmRooms.query(xid, this.rmRooms.getID(), resvKey);
                    totalBill += hotel.getPrice();
                } else if (resvType == Reservation.RESERVATION_TYPE_CAR) {
                    Car car = (Car) this.rmCars.query(xid, this.rmCars.getID(), resvKey);
                    totalBill += car.getPrice();
                }
            }
            return totalBill;
        }catch(DeadlockException e){
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }catch(InvalidIndexException iie){
            iie.printStackTrace();
        }

        return -1;
    }


    // RESERVATION INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "reserveFlight");
        }

        // check the input valid
        if (custName == null || flightNum == null) {
            return false;
        }

        try {
            ResourceItem resourceCustomer = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);
            if (resourceCustomer == null) {
                return false;
            }
            ResourceItem resourceItemFlight = this.rmFlights.query(xid, this.rmFlights.getID(), flightNum);
            if (resourceItemFlight == null) {
                return false;
            }

            Flight flight = (Flight) resourceItemFlight;
            if (!flight.addResv(1)) {
                return false;
            }

            this.rmFlights.update(xid, this.rmFlights.getID(), flightNum, flight);

            Reservation reservation = new Reservation(custName, Reservation.RESERVATION_TYPE_FLIGHT, flightNum);
            return this.rmCustomers.insert(xid, ResourceManager.TableNameReservations, reservation);

        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public boolean reserveCar(int xid, String custName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        // check if the transaction exist
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "reserveCar");
        }

        // check the input valid
        if (custName == null || location == null)
            return false;

        try {
            ResourceItem resourceCustomer = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);
            if (resourceCustomer == null) {
                return false;
            }
            ResourceItem resourceItemCar = this.rmCars.query(xid, this.rmCars.getID(), location);
            if (resourceItemCar == null) {
                return false;
            }

            Car car = (Car) resourceItemCar;
            if (!car.addResv(1)) {
                return false;
            }

            this.rmCars.update(xid, this.rmCars.getID(), location, car);

            Reservation reservation = new Reservation(custName, Reservation.RESERVATION_TYPE_CAR, location);
            return this.rmCustomers.insert(xid, ResourceManager.TableNameReservations, reservation);
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public boolean reserveRoom(int xid, String custName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "reserveRoom");
        }

        if (custName == null || location == null) {
            return false;
        }

        try {
            ResourceItem resourceCustomer = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);
            if (resourceCustomer == null) {
                return false;
            }
            ResourceItem resourceItemRoom = this.rmRooms.query(xid, this.rmRooms.getID(), location);
            if (resourceItemRoom == null) {
                return false;
            }

            Hotel hotel = (Hotel) resourceItemRoom;
            if (!hotel.addResv(1)) {
                return false;
            }

            this.rmRooms.update(xid, this.rmRooms.getID(), location, hotel);

            Reservation reservation = new Reservation(custName, Reservation.RESERVATION_TYPE_HOTEL, location);
            return this.rmCustomers.insert(xid, ResourceManager.TableNameReservations, reservation);
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    // TECHNICAL/TESTING INTERFACE
    public boolean reconnect()
            throws RemoteException {

        String rmiPort = System.getProperty("rmiPort");
        rmiPort = Utils.genrConSyntax(rmiPort);

        try {
            rmFlights =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameFlights);
            System.out.println("WC bound to RMFlights");
            rmRooms =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameRooms);
            System.out.println("WC bound to RMRooms");
            rmCars =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameCars);
            System.out.println("WC bound to RMCars");
            rmCustomers =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameCustomers);
            System.out.println("WC bound to RMCustomers");
            tm =
                    (TransactionManager) Naming.lookup(rmiPort +
                            TransactionManager.RMIName);
            System.out.println("WC bound to TM");
        } catch (Exception e) {
            System.err.println("WC cannot bind to some component:" + e);
            return false;
        }

        try {
            //TODO maybe ping is better
            if (rmFlights.reconnect() && rmRooms.reconnect() &&
                    rmCars.reconnect() && rmCustomers.reconnect()) {
                return true;
            }
        } catch (Exception e) {
            System.err.println("Some RM cannot reconnect:" + e);
            return false;
        }

        return false;
    }

    public boolean dieNow(String who)
            throws RemoteException {
        if (who.equals(TransactionManager.RMIName) ||
                who.equals("ALL")) {
            try {
                tm.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameFlights) ||
                who.equals("ALL")) {
            try {
                rmFlights.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameRooms) ||
                who.equals("ALL")) {
            try {
                rmRooms.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameCars) ||
                who.equals("ALL")) {
            try {
                rmCars.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameCustomers) ||
                who.equals("ALL")) {
            try {
                rmCustomers.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(WorkflowController.RMIName) ||
                who.equals("ALL")) {
            System.exit(1);
        }
        return true;
    }

    private boolean dieRMAt(String who, String time) throws RemoteException {
        switch (who) {
            case ResourceManager.RMINameFlights: {
                rmFlights.setDieTime(time);
                break;
            }
            case ResourceManager.RMINameCars: {
                rmCars.setDieTime(time);
                break;
            }
            case ResourceManager.RMINameCustomers: {
                rmCustomers.setDieTime(time);
                break;
            }
            case ResourceManager.RMINameRooms: {
                rmRooms.setDieTime(time);
                break;
            }
            default: {
                System.err.println("Invalid RM: " + who);
                return false;
            }
        }
        return true;
    }

    public boolean dieRMAfterEnlist(String who)
            throws RemoteException {
        return this.dieRMAt(who,"AfterEnlist");
    }

    public boolean dieRMBeforePrepare(String who)
            throws RemoteException {
        return this.dieRMAt(who,"BeforePrepare");
    }

    public boolean dieRMAfterPrepare(String who)
            throws RemoteException {
        return this.dieRMAt(who,"AfterPrepare");
    }

    public boolean dieTMBeforeCommit()
            throws RemoteException {
        tm.setDieTime("BeforeCommit");
        return true;
    }

    public boolean dieTMAfterCommit()
            throws RemoteException {
        tm.setDieTime("AfterCommit");
        return true;
    }

    public boolean dieRMBeforeCommit(String who)
            throws RemoteException {
        return this.dieRMAt(who,"BeforeCommit");
    }

    public boolean dieRMBeforeAbort(String who)
            throws RemoteException {
        return this.dieRMAt(who,"AfterAbort");
    }
}
