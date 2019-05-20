package ru.ifmo.rain.chizhikov.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

/**
 * {@link Serializable} implementation of {@link Person} interface.
 */
public class LocalPerson implements Person, Serializable {
    private String name;
    private String surname;
    private String passportID;
    private Map<String, LocalAccount> accounts;

    LocalPerson(String name, String surname, String passportID, Map<String, LocalAccount> accounts) throws RemoteException {
        super();
        this.name = name;
        this.surname = surname;
        this.passportID = passportID;
        this.accounts = accounts;
    }

    /**
     * Returns name of {@link LocalPerson}.
     *
     * @return {@link String} name of {@link LocalPerson}
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public String getName() throws RemoteException {
        return name;
    }

    /**
     * Returns surname of {@link LocalPerson}.
     *
     * @return {@link String} surname of {@link LocalPerson}
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public String getSurname() throws RemoteException {
        return surname;
    }

    /**
     * Returns passportID of {@link LocalPerson}.
     *
     * @return {@link String} passportID of {@link LocalPerson}
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public String getPassportID() throws RemoteException {
        return passportID;
    }

    /**
     * Returns one of {@link LocalPerson} {@link Account} by it's ID.
     *
     * @param id given ID.
     * @return {@link LocalPerson}'s {@link Account}
     */
    Account getAccountById(String id) {
        return accounts.get(id);
    }

    /**
     * Returns {@link Set} of {@link LocalPerson} accounts. Accounts represented as accountID.
     *
     * @return {@link Set} of {@link String} accountIDs.
     */
    Set<String> getAccounts() {
        return accounts.keySet();
    }
}
