package ru.ifmo.rain.chizhikov.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * {@link Remote} interface for implementing {@link LocalAccount} and {@link RemoteAccount}.
 */
public interface Account extends Remote {
    String getId() throws RemoteException;

    int getAmount() throws RemoteException;

    void setAmount(int amount) throws RemoteException;
}
