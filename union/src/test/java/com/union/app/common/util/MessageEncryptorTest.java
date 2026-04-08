package com.union.app.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class MessageEncryptorTest {

    private MessageEncryptor encryptor;

    @BeforeEach
    void setUp() {
        encryptor = new MessageEncryptor();

        ReflectionTestUtils.setField(encryptor, "password", "mySecretPassword123");
        ReflectionTestUtils.setField(encryptor, "salt", "deadbeef12345678");

        encryptor.init();
    }

    @Test
    void encryptAndDecrypt_shouldReturnOriginalText() {
        String original = "Hello World";

        String encrypted = encryptor.encrypt(original);
        String decrypted = encryptor.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void encrypt_shouldProduceDifferentResultsForSameInput() {
        String text = "Same message";

        String encrypted1 = encryptor.encrypt(text);
        String encrypted2 = encryptor.encrypt(text);

        assertNotEquals(encrypted1, encrypted2);

        assertEquals(text, encryptor.decrypt(encrypted1));
        assertEquals(text, encryptor.decrypt(encrypted2));
    }

    @Test
    void decrypt_shouldFailForInvalidInput() {
        String invalidEncryptedText = "not-encrypted-text";

        assertThrows(Exception.class, () -> encryptor.decrypt(invalidEncryptedText));
    }

    @Test
    void encryptAndDecrypt_emptyString() {
        String original = "";

        String encrypted = encryptor.encrypt(original);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted);
    }

    @Test
    void encryptAndDecrypt_nullHandling() {
        assertThrows(Exception.class, () -> encryptor.encrypt(null));
        assertThrows(Exception.class, () -> encryptor.decrypt(null));
    }
}