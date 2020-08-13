package test;

import transaction.WorkflowController;

import static transaction.Utils.ExitWC;
import static transaction.Utils.bindWC;

public class CRUDCreate {

    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            int xid = wc.start();
            if (!wc.addFlight(xid, "flight1", 100, 499)) {
                System.err.println("Add flight failed");
            }
            if (!wc.addRooms(xid, "room1", 99, 399)) {
                System.err.println("Add room failed");
            }
            if (!wc.addCars(xid, "car1", 89, 299000)){
                System.err.println("Add car failed");
            }

            if (!wc.newCustomer(xid, "customer1")){
                System.err.println("Add customer failed");
            }
            if (!wc.reserveFlight(xid, "customer1", "flight1")) {
                System.err.println("Reserve flight failed");
            }
            if (!wc.reserveRoom(xid, "customer1", "room1")) {
                System.err.println("Reserve room failed");
            }
            if (!wc.reserveCar(xid, "customer1", "car1")) {
                System.err.println("Reserve car failed");
            }

            if (!wc.commit(xid)){
                System.err.println("Commit failed");
            }

            System.out.println("Test pass.");
            ExitWC(wc, 0);
        } catch (Exception e) {
            System.out.println("Test fail:" + e.getMessage());
            ExitWC(wc, 0);
        }
    }
}
