package ru.ifmo.rain.chizhikov.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteAccount extends UnicastRemoteObject implements Account {

    RemoteAccount(String id, int port) throws RemoteException {
        super(port);
        this.id = id;
        this.amount = 0;
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
