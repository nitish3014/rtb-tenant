package com.rtb.tenant.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "auth")
public record AuthSigningKey(
    RSAPublicKey publicKey
) {

}