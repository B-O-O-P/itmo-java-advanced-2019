package ru.ifmo.rain.chizhikov.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;

public class LocalAccount implements Account, Serializable {

    LocalAccount(String id, int amount) throws RemoteException {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String getId() throws RemoteException {
        return id;
    }

    @Override
    public synchronized int getAmount() throws RemoteException {
        return amount;
    }

    @Override
    public synchronized void setAmount(int amount) throws RemoteException {
        this.amount = amount;
    }

    private String id;
    private int amount;
}
