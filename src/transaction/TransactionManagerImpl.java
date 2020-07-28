package transaction;

import transaction.rm.ResourceManager;

import java.io.FileInputStream;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

/**
 * Transaction Manager for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl
        extends java.rmi.server.UnicastRemoteObject
        implements TransactionManager {

    public static void main(String args[]) {
        System.setSecurityManager(new SecurityManager());

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("conf/ddb.conf"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String tmPort = properties.getProperty("tm.port");
        try {
            TransactionManagerImpl tmi = new TransactionManagerImpl();
            Registry _registry = LocateRegistry.createRegistry(Integer.parseInt(tmPort));
            _registry.bind(TransactionManager.RMIName, tmi);
            System.out.println("TM bound in port: " + tmPort);
        } catch (Exception e) {
            System.err.println("TM not bound:" + e);
            System.exit(1);
        }
//        String
//        String rmiPort = System.getProperty("rmiPort");
//        if (rmiPort == null) {
//            rmiPort = "";
//        } else if (!rmiPort.equals("")) {
//            rmiPort = "//:" + rmiPort + "/";
//        }
//
//        try {
//            TransactionManagerImpl obj = new TransactionManagerImpl();
//            Naming.rebind(rmiPort + TransactionManager.RMIName, obj);
//            System.out.println("TM bound");
//        } catch (Exception e) {
//            System.err.println("TM not bound:" + e);
//            System.exit(1);
//        }
    }

    public void ping() throws RemoteException {
    }

    public void enlist(int xid, ResourceManager rm) throws RemoteException {
    }

    public TransactionManagerImpl() throws RemoteException {
    }

    public boolean dieNow()
            throws RemoteException {
        System.exit(1);
        return true; // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
    }

}
