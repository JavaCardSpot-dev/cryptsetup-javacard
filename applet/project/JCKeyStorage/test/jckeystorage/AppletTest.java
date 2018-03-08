/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jckeystorage;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Mmabatho
 */
public class AppletTest {

    private static final byte[] UUID = new byte[40];

    private KeyStorageClient client;

    public AppletTest() {
    }

    @Before
    public void setUp() throws GeneralSecurityException {
        client = new KeyStorageClient(SimulatorSmartCardIO.INSTANCE,
                SecureRandom.getInstance("NativePRNGNonBlocking"));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void checkOutput() throws ClientException {
        client.installApplet("test".toCharArray());
        client.selectApplet();

        RSAKeyParameters publickey = client.getPublicKey();
        KeyStorageClient.Session session = client.openSession(publickey);
        session.authenticate("test".toCharArray());

        session.changeMasterPassword("test2".toCharArray());
        session.close();
        session = client.openSession(publickey);
        session.authenticate("test2".toCharArray());
        byte[] generatedKey = session.generateKey(32);
        assertEquals(32, generatedKey.length);
        session.storeKey(UUID, generatedKey);
        byte[] key = session.loadKey(UUID);
        assertArrayEquals(generatedKey, key);
        session.deleteKey(UUID);
        
        boolean failed = false;
        try {
            session.loadKey(UUID);
        } catch (Exception e) {
            failed = true;
        }
        assertEquals("Key not deleted!", true, failed);
        session.close();
    }
}
