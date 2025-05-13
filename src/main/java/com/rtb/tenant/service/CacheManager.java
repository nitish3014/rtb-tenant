package com.rtb.tenant.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheManager {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        private final String shortUrl;
        private final long expiry;

        public CacheEntry(String shortUrl, long expiry) {
            this.shortUrl = shortUrl;
            this.expiry = expiry;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiry;
        }

        public String getShortUrl() {
            return shortUrl;
        }
    }
}
