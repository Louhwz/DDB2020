package transaction;

import transaction.exception.InvalidTransactionException;
import transaction.exception.TransactionAbortedException;

import java.rmi.*;

/**
 * Interface for the Transaction Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */

public interface TransactionManager extends Remote {
    String INITED = "INITED";
    String PREPARED = "PREPARED";
    String COMMITTED = "COMMITTED";
    String ABORTED = "aborted";

    boolean dieNow()
            throws RemoteException;

    void setDieTime(String dieTime) throws RemoteException;

    void ping() throws RemoteException;

    int start() throws RemoteException;

    void enlist(int xid, ResourceManager rm) throws RemoteException, InvalidTransactionException;

    boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException;

    void abort(int xid) throws RemoteException, InvalidTransactionException;

    /**
     * The RMI name a TransactionManager binds to.
     */
    String RMIName = "TM";
}
