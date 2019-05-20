package ru.ifmo.rain.chizhikov.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * Server class for {@link Bank}.
 */
public class Server {
    private final static int PORT = 8888;
    private final static String HOST_NAME = "//localhost/bank";

    /**
     * Starts {@link Server} on default port and host.
     *
     * @param args no args.
     */
    public static void main(String[] args) {
        try {
            final Bank bank = new RemoteBank(PORT);
            Naming.rebind(HOST_NAME, bank);
        } catch (RemoteException e) {
            System.err.println("ERROR: Cannot export object: " + e.getMessage());
        } catch (MalformedURLException e) {
            System.err.println("ERROR: Malformed URL");
        }
    }
}
