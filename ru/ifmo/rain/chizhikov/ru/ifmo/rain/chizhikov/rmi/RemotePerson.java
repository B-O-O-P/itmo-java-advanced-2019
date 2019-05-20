package ru.ifmo.rain.chizhikov.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemotePerson extends UnicastRemoteObject implements Person {

    RemotePerson(String name, String surname, String passportId, int port) throws RemoteException {
        super(port);
        this.name = name;
        this.surname = surname;
        this.passportId = passportId;
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public String getSurname() throws RemoteException {
        return surname;
    }

    @Override
    public String getPassportID() throws RemoteException {
        return passportId;
    }


    private String name;
    private String surname;
    private String passportId;
}
