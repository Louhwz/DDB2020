package transaction;

import transaction.rm.ResourceManager;

import java.rmi.*;

/**
 * Interface for the Transaction Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */

public interface TransactionManager extends Remote {

    boolean dieNow()
            throws RemoteException;

    void ping() throws RemoteException;

    void enlist(int xid, ResourceManager rm) throws RemoteException;


    /**
     * The RMI name a TransactionManager binds to.
     */
    String RMIName = "TM";
}
