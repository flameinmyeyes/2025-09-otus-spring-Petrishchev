package com.union.app.common.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class MessageEncryptor {
    @Value("${encryption.password}")
    private String password;

    @Value("${encryption.salt}")
    private String salt;

    private TextEncryptor encryptor;

    @PostConstruct
    public void init() {
        encryptor = Encryptors.text(password, salt);
    }

    public String encrypt(String text) {
        return encryptor.encrypt(text);
    }

    public String decrypt(String encryptedText) {
        return encryptor.decrypt(encryptedText);
    }
}