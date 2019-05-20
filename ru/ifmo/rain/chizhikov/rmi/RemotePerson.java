package ru.ifmo.rain.chizhikov.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * {@link UnicastRemoteObject} implementation of {@link Person} interface.
 */
public class RemotePerson extends UnicastRemoteObject implements Person {
    private String name;
    private String surname;
    private String passportId;

    RemotePerson(String name, String surname, String passportId, int port) throws RemoteException {
        super(port);
        this.name = name;
        this.surname = surname;
        this.passportId = passportId;
    }

    /**
     * Returns name of {@link RemotePerson}.
     *
     * @return {@link String} name of {@link RemotePerson}
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public String getName() throws RemoteException {
        return name;
    }

    /**
     * Returns surname of {@link RemotePerson}.
     *
     * @return {@link String} surname of {@link RemotePerson}
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public String getSurname() throws RemoteException {
        return surname;
    }

    /**
     * Returns passportID of {@link RemotePerson}.
     *
     * @return {@link String} passportID of {@link RemotePerson}
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public String getPassportID() throws RemoteException {
        return passportId;
    }
}
