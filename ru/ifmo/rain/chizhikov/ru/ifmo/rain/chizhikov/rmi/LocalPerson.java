package ru.ifmo.rain.chizhikov.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

public class LocalPerson implements Person, Serializable {

    LocalPerson(String name, String surname, String passportID, Map<String, LocalAccount> accounts) throws RemoteException {
        super();
        this.name = name;
        this.surname = surname;
        this.passportID = passportID;
        this.accounts = accounts;
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
        return passportID;
    }

    Account getAccountById(String id) {
        return accounts.get(id);
    }

    Set<String> getAccounts() {
        return accounts.keySet();
    }

    private String name;
    private String surname;
    private String passportID;
    private Map<String, LocalAccount> accounts;
}
