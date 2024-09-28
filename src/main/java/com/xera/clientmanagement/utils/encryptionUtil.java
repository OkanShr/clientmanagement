package com.xera.clientmanagement.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

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

    public Object encryptAllFields(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(object);

                // Check if the field is of type String, Integer (int), or Date
                if (fieldValue != null) {
                    if (field.getType() == String.class) {
                        // Encrypt String fields
                        String encryptedValue = encrypt((String) fieldValue); // Assuming encrypt() is defined
                        field.set(object, encryptedValue);
                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                        // Encrypt Integer fields
                        String encryptedValue = encrypt(String.valueOf(fieldValue)); // Convert int to String for encryption
                        field.set(object, Integer.valueOf(encryptedValue)); // Convert back to Integer
                    } else if (field.getType() == Date.class) {
                        // Encrypt Date fields
                        String encryptedValue = encrypt(fieldValue.toString()); // Convert Date to String
                        field.set(object, new SimpleDateFormat("yyyy-MM-dd").parse(encryptedValue)); // Parse encrypted string back to Date
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions appropriately
            }
        }
        return object;
    }

    public Object decryptAllFields(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(object);

                // Check if the field is of type String, Integer (int), or Date
                if (fieldValue != null) {
                    if (field.getType() == String.class) {
                        // Decrypt String fields
                        String decryptedValue = decrypt((String) fieldValue); // Assuming decrypt() is defined
                        field.set(object, decryptedValue);
                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                        // Decrypt Integer fields
                        String decryptedValue = decrypt(String.valueOf(fieldValue)); // Decrypt as String
                        field.set(object, Integer.valueOf(decryptedValue)); // Convert decrypted String back to Integer
                    } else if (field.getType() == Date.class) {
                        // Decrypt Date fields
                        String decryptedValue = decrypt(fieldValue.toString()); // Decrypt as String
                        field.set(object, new SimpleDateFormat("yyyy-MM-dd").parse(decryptedValue)); // Convert decrypted String back to Date
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions appropriately
            }
        }
        return object;
    }

}
