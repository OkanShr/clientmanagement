package com.xera.clientmanagement.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.Base64;

import com.xera.clientmanagement.config.EncryptionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class encryptionUtil {

    private final String algorithm;
    private final SecretKey secretKey;

    @Autowired
    public encryptionUtil(EncryptionConfig encryptionConfig) {
        this.algorithm = encryptionConfig.getAlgorithm();
        this.secretKey = new SecretKeySpec(encryptionConfig.getKey().getBytes(), algorithm);
    }

    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error occurred while encrypting data", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error occurred while decrypting data", e);
        }
    }

    public <T> T encryptAllFields(T object) {
        try {
            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType() == String.class) {
                    String value = (String) field.get(object);
                    if (value != null) {
                        field.set(object, encrypt(value));
                    }
                }
            }
            return object;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T decryptAllFields(T object) {
        try {
            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType() == String.class) {
                    String value = (String) field.get(object);
                    if (value != null) {
                        field.set(object, decrypt(value));
                    }
                }
            }
            return object;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
