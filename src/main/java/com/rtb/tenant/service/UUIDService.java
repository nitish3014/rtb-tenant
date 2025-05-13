package com.rtb.tenant.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UUIDService {
    public String generateRandomUUID() {
        // Generate a random UUID
        UUID randomUUID = UUID.randomUUID();

        // Convert the UUID to a string and return it
        return randomUUID.toString();
    }
}
