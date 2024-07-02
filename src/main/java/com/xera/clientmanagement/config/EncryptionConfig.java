package com.xera.clientmanagement.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class EncryptionConfig {

    @Value("${encryption.algorithm}")
    private String algorithm;

    @Value("${encryption.key}")
    private String key;

}
