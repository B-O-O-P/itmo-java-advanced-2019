package ru.ifmo.rain.chizhikov.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class RemoteBank extends UnicastRemoteObject implements Bank {

    RemoteBank(int port) throws RemoteException {
        super(port);
        this.port = port;
    }

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

    @Override
    public Person getRemotePerson(String passportId) throws RemoteException {
        if (passportId != null) {
            return persons.get(passportId);
        } else {
            return null;
        }
    }

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

    private int port;
    private ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Set<String>> accountsByPassportID = new ConcurrentHashMap<>();
}
