package ru.ifmo.rain.chizhikov.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * {@link Remote} interface for implementing {@link LocalPerson} and {@link RemotePerson}.
 */
public interface Person extends Remote {
    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    String getPassportID() throws RemoteException;
}
