package ru.ifmo.rain.chizhikov.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
    public static void main(String[] args) {
        final Bank bank;

        try {
            bank = (Bank) Naming.lookup(HOST_NAME);
        } catch (NotBoundException e) {
            System.err.println("ERROR: Bank is not bound");
            return;
        } catch (MalformedURLException e) {
            System.err.println("ERROR: Bank URL is invalid");
            return;
        } catch (RemoteException e) {
            System.err.println("REMOTE ERROR: " + e.getMessage());
            return;
        }

        String name;
        String surname;
        String passportId;
        String accountId;
        int change;

        try {
            name = args[0];
            surname = args[1];
            passportId = args[2];
            accountId = args[3];
            change = Integer.parseInt(args[4]);
        } catch (Exception e) {
            System.err.println("ERROR: Invalid arguments.\n" +
                    "Usage: <name> <surname> <passport> <account id> <change>");
            return;
        }

        try {
            Person person = bank.getRemotePerson(passportId);

            if (person != null) {
                if (!bank.getPersonAccounts(person).contains(accountId)) {
                    Account account = bank.getAccount(accountId, person);

                    if (account != null) {
                        System.err.println("ERROR: Account already exists");
                        return;
                    } else {
                        bank.createAccount(accountId, person);
                    }
                }
            } else {
                bank.createPerson(name, surname, passportId);
            }

            Account account = bank.getAccount(accountId, person);

            System.out.println("BANK ACCOUNT INFORMATION:" +
                    "\nID: " + account.getId() +
                    "\nBALANCE: " + account.getAmount());
            System.out.println("Recounting money...");
            System.out.println("Changing account balance...");

            account.setAmount(account.getAmount() + change);

            System.out.println("Balance changed:\n" +
                    "BALANCE: " + account.getAmount());

        } catch (RemoteException e) {
            System.err.println("REMOTE ERROR: " + e.getMessage());
        }

    }

    private final static String HOST_NAME = "//localhost/bank";
}
