package ru.ifmo.rain.chizhikov.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class RemoteBank extends UnicastRemoteObject implements Bank {
    private int port;
    private ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Set<String>> accountsByPassportID = new ConcurrentHashMap<>();

    RemoteBank(int port) throws RemoteException {
        super(port);
        this.port = port;
    }

    /**
     * Creates {@link Person}'s {@link Account} with given accountID.
     *
     * @param subId given accountID
     * @param person given {@link Person}
     * @return {@link Boolean} true if {@link Account} created, false otherwise
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public boolean createAccount(String subId, Person person) throws RemoteException {
        if (subId != null && person != null) {
            String accountId = person.getPassportID() + ":" + subId;

            if (!accounts.containsKey(accountId)) {
                Account account = new RemoteAccount(subId, port);

                accounts.put(accountId, account);

                if (accountsByPassportID.get(person.getPassportID()) == null) {
                    accountsByPassportID.put(person.getPassportID(), new ConcurrentSkipListSet<>());
                }

                accountsByPassportID.get(person.getPassportID()).add(subId);

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns {@link Account} by given accountID and {@link Person}.
     *
     * @param subId given accountID
     * @param person given {@link Person}
     * @return {@link Account} if one is found, {@code null} otherwise
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public Account getAccount(String subId, Person person) throws RemoteException {
        if (subId != null && person != null) {
            String accountID = person.getPassportID() + ":" + subId;
            Account account = accounts.get(accountID);

            if (account != null) {

                if (person instanceof LocalPerson) {
                    return ((LocalPerson) person).getAccountById(subId);
                } else {
                    return account;
                }

            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Creates {@link Person} by given name, surname and passportID.
     *
     * @param name given name
     * @param surname given surname
     * @param passportId given passportID.
     * @return {@link Boolean} true if {@link Person} created, false otherwise
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public boolean createPerson(String name, String surname, String passportId) throws RemoteException {
        if (name != null && surname != null && persons.get(passportId) == null) {
            persons.put(passportId, new RemotePerson(name, surname, passportId, port));
            accountsByPassportID.put(passportId, new ConcurrentSkipListSet<>());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Looking for {@link Person} by given name, surname and passportID.
     *
     * @param name given name
     * @param surname given surname
     * @param passportId given passportID
     * @return {@link Boolean} true if {@link Person} found, false otherwise
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public boolean searchPerson(String name, String surname, String passportId) throws RemoteException {
        if (name != null && surname != null && passportId != null) {
            Person person = persons.get(passportId);
            return person != null &&
                    person.getName().equals(name) &&
                    person.getSurname().equals(surname);
        } else {
            return false;
        }
    }

    /**
     * Returns {@code Local} {@link Person} by it's passportID.
     *
     * @param passportId given passportID.
     * @return  {@link Person} if it's found, null otherwise
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public Person getLocalPerson(String passportId) throws RemoteException {
        if (passportId != null) {
            Person person = persons.get(passportId);

            if (person != null) {
                Map<String, LocalAccount> accounts = new ConcurrentHashMap<>();

                Set<String> personAccounts = getPersonAccounts(person);
                personAccounts.forEach((accountID) -> {
                    try {
                        Account current = getAccount(accountID, person);
                        accounts.put(accountID, new LocalAccount(current.getId(), current.getAmount()));
                    } catch (RemoteException e) {
                        System.err.println("ERROR: Unable to create local account." + e.getMessage());
                    }
                });

                return new LocalPerson(person.getName(), person.getSurname(), person.getPassportID(), accounts);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns {@code Remote} {@link Person} by it's passportID.
     *
     * @param passportId given passportID.
     * @return  {@link Person} if it's found, null otherwise
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public Person getRemotePerson(String passportId) throws RemoteException {
        if (passportId != null) {
            return persons.get(passportId);
        } else {
            return null;
        }
    }

    /**
     * Returns {@link Set} of {@link Person}'s {@link Account}s.
     *
     * @param person given {@link Person}
     * @return {@link Set} of {@link Account}s
     * @throws RemoteException if one of communication-related exceptions is thrown
     */
    @Override
    public Set<String> getPersonAccounts(Person person) throws RemoteException {
        if (person != null) {
            if (person instanceof LocalPerson) {
                return ((LocalPerson) person).getAccounts();
            } else {
                return accountsByPassportID.get(person.getPassportID());
            }
        } else {
            return null;
        }
    }
}
