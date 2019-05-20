package ru.ifmo.rain.chizhikov.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * {@link Serializable} implementation of {@link Account} interface.
 */
public class LocalAccount implements Account, Serializable {
    private String id;
    private int amount;

    LocalAccount(String id, int amount) throws RemoteException {
        this.id = id;
        this.amount = amount;
    }

    /**
     * Returns ID of {@link LocalAccount}.
     *
     * @return {@link String} ID of {@link LocalAccount}
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public String getId() throws RemoteException {
        return id;
    }

    /**
     * Returns amount of money of {@link LocalAccount}. Method is synchronized and can be resolved in parallel {@link Thread}'s.
     *
     * @return {@link Integer} amount of money of {@link LocalAccount}
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public synchronized int getAmount() throws RemoteException {
        return amount;
    }

    /**
     * Set amount of money of {@link LocalAccount}. Method is synchronized and can be resolved in parallel {@link Thread}'s.
     *
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public synchronized void setAmount(int amount) throws RemoteException {
        this.amount = amount;
    }
}
