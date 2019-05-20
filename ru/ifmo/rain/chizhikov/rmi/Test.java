package ru.ifmo.rain.chizhikov.rmi;

import org.junit.BeforeClass;
import org.junit.Assert;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.Set;

/**
 * Class for {@link org.junit.Test} {@link Bank} class.
 */
public class Test {
    private static Bank bank;
    private static final int PORT = 8888;
    private static final String HOST_NAME = "//localhost/bank";
    private static final int PERSONS_AMOUNT = 20;

    /**
     * Starting {@link Bank} instance.
     *
     * @throws Exception any exception.
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        Naming.rebind(HOST_NAME, new RemoteBank(PORT));
        bank = (Bank) Naming.lookup(HOST_NAME);
    }

    /**
     * Testing {@link Bank} {@code getLocalPerson} and {@code getRemotePerson} methods.
     *
     * @throws RemoteException thrown from one of tested methods.
     */
    @org.junit.Test
    public void getPersonTest() throws RemoteException {
        Assert.assertNull(bank.getLocalPerson(Integer.toString(-1)));
        Assert.assertNull(bank.getRemotePerson(Integer.toString(-1)));

        for (int i = 0; i < PERSONS_AMOUNT; ++i) {
            bank.createPerson("person" + i, "", "person" + i);

            Person remotePerson = bank.getRemotePerson("person" + i);
            Assert.assertEquals("person" + i, remotePerson.getName());
            Assert.assertEquals("", remotePerson.getSurname());
            Assert.assertEquals("person" + i, remotePerson.getPassportID());

            Person localPerson = bank.getLocalPerson("person" + i);
            Assert.assertEquals("person" + i, localPerson.getName());
            Assert.assertEquals("", localPerson.getSurname());
            Assert.assertEquals("person" + i, localPerson.getPassportID());
        }
    }

    /**
     * Testing {@link Bank} {@code searchPerson}.
     *
     * @throws RemoteException thrown from tested method.
     */
    @org.junit.Test
    public void seacrhAndCreatePersonTest() throws RemoteException {
        for (int i = 0; i < PERSONS_AMOUNT; ++i) {
            Assert.assertFalse(bank.searchPerson("nonexistent" + i, "", "nonexistent" + i));
            Assert.assertTrue(bank.createPerson("nonexistent" + i, "", "nonexistent" + i));
            Assert.assertTrue(bank.searchPerson("nonexistent" + i, "", "nonexistent" + i));
        }
    }

    /**
     * Testing {@link Bank} {@code getPersonAccounts} method.
     *
     * @throws RemoteException thrown from tested method.
     */
    @org.junit.Test
    public void getAccountsTest() throws RemoteException {
        for (int i = 0; i < PERSONS_AMOUNT; ++i) {
            Assert.assertTrue(bank.createPerson("getAccounts", Integer.toString(i), "getAccounts" + i));

            Random random = new Random();
            int done = 0;

            Person remotePerson = bank.getRemotePerson("getAccounts" + i);
            for (int j = 0; j < random.nextInt(PERSONS_AMOUNT); ++j) {
                if (bank.createAccount(Integer.toString(random.nextInt()), remotePerson)) {
                    done++;
                }
            }

            Set<String> accounts = bank.getPersonAccounts(remotePerson);
            Assert.assertNotNull(accounts);
            Assert.assertEquals(done, accounts.size());
        }
    }

    /**
     * Testing {@link Bank} {@code createAccount} method.
     *
     * @throws RemoteException thrown from tested method.
     */
    @org.junit.Test
    public void creatingAccountTest() throws RemoteException {
        bank.createPerson("createdAccount", "1", "createdAccount1");
        Person localPerson = bank.getLocalPerson("createdAccount1");
        Person remotePerson = bank.getRemotePerson("createdAccount1");

        bank.createAccount("2", remotePerson);
        Assert.assertNull(bank.getAccount("2", localPerson));
        Assert.assertEquals(1, bank.getPersonAccounts(remotePerson).size());
        Assert.assertNotNull(bank.getAccount("2", remotePerson));
        Assert.assertNotEquals(bank.getPersonAccounts(localPerson), bank.getPersonAccounts(remotePerson));
    }

    /**
     * Testing {@link Bank} sequence of requests - {@code Remote} after {@code Local}.
     *
     * @throws RemoteException thrown from one of tested methods.
     */
    @org.junit.Test
    public void remoteAfterLocalTest() throws RemoteException {
        bank.createPerson("remoteAfterLocal", "1", "remoteAfterLocal1");

        Person remotePerson = bank.getRemotePerson("remoteAfterLocal1");
        Assert.assertNotNull(remotePerson);

        Assert.assertTrue(bank.createAccount("1", remotePerson));
        Person localPerson = bank.getLocalPerson("remoteAfterLocal1");
        Assert.assertNotNull(localPerson);

        Account localAccount = bank.getAccount("1", localPerson);
        localAccount.setAmount(localAccount.getAmount() + 100000);

        Account remoteAccount = bank.getAccount("1", remotePerson);

        Assert.assertEquals(100000, localAccount.getAmount());
        Assert.assertEquals(0, remoteAccount.getAmount());
    }

    /**
     * Testing {@link Bank} sequence of requests - {@code Local} after {@code Remote}.
     *
     * @throws RemoteException thrown from one of tested methods.
     */
    @org.junit.Test
    public void localAfterRemoteTest() throws RemoteException {
        bank.createPerson("localAfterRemote", "1", "localAfterRemote1");
        Person remotePerson = bank.getRemotePerson("localAfterRemote1");

        Assert.assertNotNull(remotePerson);
        Assert.assertTrue(bank.createAccount("1", remotePerson));
        Account remoteAccount = bank.getAccount("1", remotePerson);

        Person localPerson1 = bank.getLocalPerson("localAfterRemote1");
        Assert.assertNotNull(localPerson1);

        remoteAccount.setAmount(remoteAccount.getAmount() + 100000);

        Person localPerson2 = bank.getLocalPerson("localAfterRemote1");
        Assert.assertNotNull(localPerson2);

        Account localAccount1 = bank.getAccount("1", localPerson2);
        Account localAccount2 = bank.getAccount("1", localPerson1);

        Assert.assertEquals(localAccount1.getAmount(), remoteAccount.getAmount());
        Assert.assertEquals(localAccount2.getAmount() + 100000, localAccount1.getAmount());
    }

    /**
     * Testing {@link Bank} sequence of requests - {@code Local} after {@code Local}.
     *
     * @throws RemoteException thrown from one of tested methods.
     */
    @org.junit.Test
    public void localAfterLocal() throws RemoteException {
        bank.createPerson("localAfterLocal", "1", "localAfterLocal1");

        Person localPerson1 = bank.getLocalPerson("localAfterLocal1");
        Person localPerson2 = bank.getLocalPerson("localAfterLocal1");

        bank.createAccount("1", localPerson1);
        bank.createAccount("2", localPerson2);

        Person localPerson3 = bank.getLocalPerson("localAfterLocal1");

        Assert.assertEquals(2, bank.getPersonAccounts(localPerson3).size());
        Assert.assertEquals(0, bank.getPersonAccounts(localPerson1).size());
        Assert.assertEquals(bank.getPersonAccounts(localPerson1).size(), bank.getPersonAccounts(localPerson2).size());
    }

    /**
     * Testing {@link Bank} sequence of requests - {@code Remote} after {@code Remote}.
     *
     * @throws RemoteException thrown from one of tested methods.
     */
    @org.junit.Test
    public void remoteAfterRemote() throws RemoteException {
        bank.createPerson("remoteAfterRemote", "1", "remoteAfterRemote1");

        Person remotePerson1 = bank.getRemotePerson("remoteAfterRemote1");
        Person remotePerson2 = bank.getRemotePerson("remoteAfterRemote1");

        bank.createAccount("1", remotePerson1);
        bank.createAccount("2", remotePerson2);

        Assert.assertEquals(2, bank.getPersonAccounts(remotePerson1).size());
        Assert.assertEquals(bank.getPersonAccounts(remotePerson1).size(), bank.getPersonAccounts(remotePerson2).size());
    }
}
