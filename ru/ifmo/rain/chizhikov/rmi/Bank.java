package ru.ifmo.rain.chizhikov.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * {@link Remote} interface for creating remote server-client Bank.
 */
public interface Bank extends Remote {
    boolean createAccount(String subId, Person person) throws RemoteException;

    Account getAccount(String subId, Person person) throws RemoteException;


    boolean createPerson(String name, String surname, String passportId) throws RemoteException;

    boolean searchPerson(String name, String surname, String passportId) throws RemoteException;

    Person getLocalPerson(String passportId) throws RemoteException;

    Person getRemotePerson(String passportId) throws RemoteException;

    Set<String> getPersonAccounts(Person person) throws RemoteException;
}
