package io.budgetapp.crypto;

import java.security.SecureRandom;

/**
 *
 */
public class SaltGenerator {

    private static final int DEFAULT_KEY_LENGTH = 8;

    private final SecureRandom random;

    public SaltGenerator() {
        this.random = new SecureRandom();
    }

    public byte[] generateKey() {
        byte[] bytes = new byte[DEFAULT_KEY_LENGTH];
        random.nextBytes(bytes);
        return bytes;
    }


    public int getKeyLength() {
        return DEFAULT_KEY_LENGTH;
    }
}
