package com.xera.clientmanagement.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xera.clientmanagement.config.EncryptionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class encryptionUtil {

    private final String algorithm;
    private final SecretKey secretKey;
    private static final Logger log = LoggerFactory.getLogger(encryptionUtil.class);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    public String encryptLocalDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        String dateString = localDate.format(dateFormatter);
        return encrypt(dateString); // Use your existing encrypt method
    }

    public LocalDate decryptLocalDate(String encryptedDate) {
        if (encryptedDate == null) {
            return null;
        }
        String decryptedDateString = decrypt(encryptedDate); // Use your existing decrypt method
        return LocalDate.parse(decryptedDateString, dateFormatter);
    }

    public Object encryptAllFields(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Consistent date format

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(object);

                // Skip null, static, or final fields
                if (fieldValue != null && !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                    if (field.getType() == String.class) {
                        // Encrypt String fields
                        String encryptedValue = encrypt((String) fieldValue);
                        field.set(object, encryptedValue);
                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                        // Encrypt Integer fields (store as strings after encryption)
                        String encryptedValue = encrypt(String.valueOf(fieldValue)); // Encrypt integer as string
                        field.set(object, encryptedValue); // Store as a string
                    }
                }
            } catch (Exception e) {
                log.error("Error encrypting field: " + field.getName(), e);
                throw new RuntimeException("Encryption failed for field: " + field.getName(), e);
            }
        }
        return object;
    }


    public Object decryptAllFields(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Consistent date format

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(object);

                // Skip null, static, or final fields
                if (fieldValue != null && !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                    if (field.getType() == String.class) {
                        // Decrypt String fields
                        String decryptedValue = decrypt((String) fieldValue);
                        field.set(object, decryptedValue);
                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                        // Decrypt Integer fields (consider storing as strings after decryption)
                        String decryptedValue = decrypt(fieldValue.toString()); // Decrypt as string
                        field.set(object, Integer.valueOf(decryptedValue)); // Convert to integer if encryption produces numeric values
                    } else if (field.getType() == Date.class) {
                        // Decrypt Date fields
                        String decryptedValue = decrypt(dateFormat.format((Date) fieldValue)); // Format date before decrypting
                        field.set(object, dateFormat.parse(decryptedValue)); // Parse back to date
                    }
                }
            } catch (Exception e) {
                log.error("Error decrypting field: " + field.getName(), e);
                throw new RuntimeException("Decryption failed for field: " + field.getName(), e);
            }
        }
        return object;
    }


}
