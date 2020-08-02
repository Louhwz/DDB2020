package transaction;

import transaction.exception.InvalidTransactionException;
import transaction.exception.TransactionAbortedException;

import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * Transaction Manager for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl
        extends java.rmi.server.UnicastRemoteObject
        implements TransactionManager {

    // in single tm, using xidNum with file record can ensure uniquness
    private int xidNum;

    private String dieTime;

    public TransactionManagerImpl() throws RemoteException {
        dieTime = "NoDie";
    }

    public static void main(String args[]) {
        System.setSecurityManager(new SecurityManager());

        String rmiPort = System.getProperty("rmiPort");
        rmiPort = Utils.genrConSyntax(rmiPort);
        try {
            TransactionManagerImpl tmi = new TransactionManagerImpl();
            Naming.rebind(rmiPort + TransactionManager.RMIName, tmi);
            System.out.println("TM bound!");
        } catch (Exception e) {
            System.err.println("TM not bound:" + e);
            System.exit(1);
        }
    }


    public void enlist(int xid, ResourceManager rm) throws RemoteException {
        System.out.println("hello world");
    }

    public boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        return false;
    }

    public void ping() throws RemoteException {
    }

    public boolean dieNow()
            throws RemoteException {
        System.exit(1);
        return true; // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
    }

}
