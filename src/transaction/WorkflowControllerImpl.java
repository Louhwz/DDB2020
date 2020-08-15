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

import static transaction.Utils.TM_DIE_AFTER_COMMIT;
import static transaction.Utils.TM_DIE_BEFORE_COMMIT;


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

    protected ResourceManager rmFlights = null;
    protected ResourceManager rmRooms = null;
    protected ResourceManager rmCars = null;
    protected ResourceManager rmCustomers = null;
    protected TransactionManager tm = null;

    private HashSet<Integer> xids = null; // store the ongoing transaction ids

    private String WC_XID_FILEPATH = "data/WC_xids.log"; // store the ongoing transaction ids on the disk, for recover from die

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

        // initialize the set of onging ids
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
        // load unfinished xids from file
        Object obj = Utils.loadObject(WC_XID_FILEPATH);

        if (obj != null) {
            this.xids = (HashSet<Integer>)obj;
        }
    }


    private void updateXIDFile(){
        // store the xids to the file
        Utils.storeObject(WC_XID_FILEPATH,this.xids);

    }


    // TRANSACTION INTERFACE
    public int start()
            throws RemoteException {

        // TM will return a xid as the transaction id
        int xid = tm.start();
        this.xids.add(xid);

        //every change should be stored in file, in case sudden die out
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

        // transaction is aborted thus finished ,should be removed
        xids.remove(xid);
        this.updateXIDFile();
    }


    private void xidIsOngoing(int xid,String info)
        throws InvalidTransactionException{
        // if there doesn't exist this transaction, throw Invalid Transaction Exception
        if (!this.xids.contains(xid)) {
            throw new InvalidTransactionException(xid, "addFlight");
        }
    }




    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        this.xidIsOngoing(xid,"addFlight");

        // check the flightNum and the numSeats valid
        if (flightNum == null || numSeats < 0) {
            return false;
        }


        try {
            // find if there already exists flightNum
            ResourceItem resourceItem = this.rmFlights.query(xid, this.rmFlights.getID(), flightNum);

            // new flight, call RM to insert new flight
            if (resourceItem == null) {
                // price is negative , change price to zero
                if(price < 0){ price = 0; }

                // add new flight
                Flight flight = new Flight(flightNum, price, numSeats, numSeats);
                return this.rmFlights.insert(xid, this.rmFlights.getID(), flight);
            }else{
                // existing flight, add the seats and overwrite price
                Flight flight = (Flight) resourceItem;
                flight.addSeats(numSeats);

                // if price is negative, no need to change the price
                if (price >= 0){
                    flight.setPrice(price);
                }

                return this.rmFlights.update(xid, this.rmFlights.getID(), flightNum, flight);
            }
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }

    }

    public boolean deleteFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        this.xidIsOngoing(xid,"deleteFlight");

        //check if the flightNum is valid
        if (flightNum == null) {
            return false;
        }

        try {
            ResourceItem resourceItem = this.rmFlights.query(xid, this.rmFlights.getID(), flightNum);

            // the flight doesn't exist
            if (resourceItem == null) {
                return false;
            }

            // find all the reservations related to the deleted flight
            Collection reservations = this.rmCustomers.query(xid, ResourceManager.TableNameReservations, Reservation.INDEX_RESERV_KEY, flightNum);

            // the flight has related reservation, cannot be deleted
            if (!reservations.isEmpty()) {
                return false;
            }

            return this.rmFlights.delete(xid, this.rmFlights.getID(), flightNum);

        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        } catch(InvalidIndexException iie){
            iie.printStackTrace();
        }

        return false;

    }

    public boolean addRooms(int xid, String location, int numRooms, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        this.xidIsOngoing(xid,"addRooms");

        // check if the location and numRooms valid
        if (location == null || numRooms < 0) {
            return false;
        }


        try {
            // check if the hotel already exist at the location
            ResourceItem resourceItem = this.rmRooms.query(xid, this.rmRooms.getID(), location);

            // new hotel
            if (resourceItem == null) {
                Hotel hotel = new Hotel(location, price, numRooms, numRooms);
                return this.rmRooms.insert(xid, this.rmRooms.getID(), hotel);
            }

            // existing hotel
            Hotel hotel = (Hotel) resourceItem;
            hotel.addRooms(numRooms);

            if (price >= 0) {
                hotel.setPrice(price);
            }

            return this.rmRooms.update(xid, this.rmRooms.getID(), location, hotel);

        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }


    public boolean deleteRooms(int xid, String location, int numRooms)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        this.xidIsOngoing(xid,"deleteRooms");

        // check if the input is valid
        if (location == null || numRooms < 0) {
            return false;
        }

        try {
            ResourceItem resourceItem = this.rmRooms.query(xid, this.rmRooms.getID(), location);
            // the hotel doesn't exist
            if (resourceItem == null) {
                return false;
            }

            Hotel hotel = (Hotel) resourceItem;
            if(!hotel.reduceRooms(numRooms)){
                return false;
            }

            return this.rmRooms.update(xid, this.rmRooms.getID(), location, hotel);

        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public boolean addCars(int xid, String location, int numCars, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        this.xidIsOngoing(xid,"addCars");

        // check if the input is valid
        if (location == null || numCars < 0) {
            return false;
        }


        try {

            //find the car is existing or not
            ResourceItem resourceItem = this.rmCars.query(xid, this.rmCars.getID(), location);

            //new car
            if (resourceItem == null) {
                Car car = new Car(location, price, numCars, numCars);

                return this.rmCars.insert(xid, this.rmCars.getID(), car);
            }

            //existing car
            Car car = (Car) resourceItem;
            car.addCars(numCars);

            if(price >= 0) {
                car.setPrice(price);
            }

            return this.rmCars.update(xid, this.rmCars.getID(), location, car);

        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }


    }

    public boolean deleteCars(int xid, String location, int numCars)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        this.xidIsOngoing(xid,"deleteCars");

        // check if the input is valid
        if (location == null || numCars < 0) {
            return false;
        }

        try {
            ResourceItem resourceItem = this.rmCars.query(xid, this.rmCars.getID(), location);

            // the car doesn't exist
            if (resourceItem == null) {
                return false;
            }

            // existing car
            Car car = (Car) resourceItem;

            // the deleted num is less than the available num
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

        this.xidIsOngoing(xid,"newCustomer");

        //check if the input is valid
        if (custName == null) {
            return false;
        }

        try {
            ResourceItem resourceItem = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);

            // the customer already exists
            if (resourceItem != null) {
                return true;
            }

            // new customer
            Customer customer = new Customer(custName);
            return this.rmCustomers.insert(xid, this.rmCustomers.getID(), customer);
        } catch (DeadlockException e) {
            this.abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public boolean deleteCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        this.xidIsOngoing(xid,"deleteCustomer");

        // check if the input is valid
        if (custName == null) {
            return false;
        }

        try {
            ResourceItem resourceItem = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);

            // the customer doesn't exist
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

            //delete the customer
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

        this.xidIsOngoing(xid,"queryFlight");

        // check the input valid
        if (flightNum == null) {
            return -1;
        }

        try {
            ResourceItem resourceItem = this.rmFlights.query(xid, this.rmFlights.getID(), flightNum);

            // the flight doesn't exist
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

        this.xidIsOngoing(xid,"queryFlightPrice");

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

        this.xidIsOngoing(xid,"queryRooms");

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

        this.xidIsOngoing(xid,"queryRoomsPrice");

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

        this.xidIsOngoing(xid,"queryCars");

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
        this.xidIsOngoing(xid,"queryCarsPrice");

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

        this.xidIsOngoing(xid,"queryCustomerBill");

        // check the input valid
        if (custName == null) {
            return -1;
        }
        try {
            ResourceItem resourceItem = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);

            // the customer doesn't exist
            if (resourceItem == null)
                return -1;

            // find all reservations related to the customer
            Collection<ResourceItem> results = null;
            results = rmCustomers.query(xid, ResourceManager.TableNameReservations, Reservation.INDEX_CUSTNAME, custName);

            // no reservations
            if (results == null)
                return 0;

            // add all the reservation price
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

        this.xidIsOngoing(xid,"reserveFlight");

        // check the input valid
        if (custName == null || flightNum == null) {
            return false;
        }

        try {
            //make sure the customer exists
            ResourceItem resourceCustomer = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);
            if (resourceCustomer == null) {
                return false;
            }

            //make sure the flight exists
            ResourceItem resourceItemFlight = this.rmFlights.query(xid, this.rmFlights.getID(), flightNum);
            if (resourceItemFlight == null) {
                return false;
            }

            // make sure the flight has enough available seat
            Flight flight = (Flight) resourceItemFlight;
            if (!flight.addResv(1)) {
                return false;
            }

            // update the flight
            this.rmFlights.update(xid, this.rmFlights.getID(), flightNum, flight);

            // update reservations
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

        this.xidIsOngoing(xid,"reserveCar");

        // check the input valid
        if (custName == null || location == null)
            return false;

        try {
            //make sure the customer exists
            ResourceItem resourceCustomer = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);
            if (resourceCustomer == null) {
                return false;
            }

            //make sure the car exists
            ResourceItem resourceItemCar = this.rmCars.query(xid, this.rmCars.getID(), location);
            if (resourceItemCar == null) {
                return false;
            }

            //is there enough cars
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
        this.xidIsOngoing(xid,"reserveRoom");

        // check the input is valid
        if (custName == null || location == null) {
            return false;
        }

        try {
            // make sure the customer exists
            ResourceItem resourceCustomer = this.rmCustomers.query(xid, this.rmCustomers.getID(), custName);
            if (resourceCustomer == null) {
                return false;
            }

            //make sure the hotel exists
            ResourceItem resourceItemRoom = this.rmRooms.query(xid, this.rmRooms.getID(), location);
            if (resourceItemRoom == null) {
                return false;
            }

            // the hotel has enough room or not
            Hotel hotel = (Hotel) resourceItemRoom;
            if (!hotel.addResv(1)) {
                return false;
            }

            // update the hotel
            this.rmRooms.update(xid, this.rmRooms.getID(), location, hotel);

            //update reservations
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
        // control when to let RM dies
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
        tm.setDieTime(TM_DIE_BEFORE_COMMIT);
        return true;
    }

    public boolean dieTMAfterCommit()
            throws RemoteException {
        tm.setDieTime(TM_DIE_AFTER_COMMIT);
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
