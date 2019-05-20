package ru.ifmo.rain.chizhikov.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * {@link UnicastRemoteObject} implementation of {@link Account} interface.
 */
public class RemoteAccount extends UnicastRemoteObject implements Account {
    private String id;
    private int amount;

    RemoteAccount(String id, int port) throws RemoteException {
        super(port);
        this.id = id;
        this.amount = 0;
    }

    /**
     * Returns ID of {@link RemoteAccount}.
     *
     * @return {@link String} ID of {@link RemoteAccount}
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public String getId() throws RemoteException {
        return id;
    }

    /**
     * Returns amount of money of {@link RemoteAccount}. Method is synchronized and can be resolved in parallel {@link Thread}'s.
     *
     * @return {@link Integer} amount of money of {@link RemoteAccount}
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public synchronized int getAmount() throws RemoteException {
        return amount;
    }

    /**
     * Set amount of money of {@link RemoteAccount}. Method is synchronized and can be resolved in parallel {@link Thread}'s.
     *
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public synchronized void setAmount(int amount) throws RemoteException {
        this.amount = amount;
    }
}
