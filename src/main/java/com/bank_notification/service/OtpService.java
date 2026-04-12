package com.bank_notification.service;

import java.time.Instant;

import java.util.Map;

import java.util.Random;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

 

/**

* Small in-memory OTP store. Suitable for dev/testing.

* Keys normalized to lowercase trimmed email addresses.

*/

@Service

public class OtpService {

 

    private static class Entry {

        final String otp;

        final Instant expiresAt;

        Entry(String otp, Instant expiresAt) { this.otp = otp; this.expiresAt = expiresAt; }

    }

 

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    private final Random rnd = new Random();

 

    /**

     * Generate an N-digit numeric OTP (digits >= 4). TTL in seconds.

     * Returns generated otp for debugging.

     */

    public String generateOtp(String key, int digits, int ttlSeconds) {

        if (key == null) throw new IllegalArgumentException("key required");

        if (digits < 4) digits = 4;

        int min = (int)Math.pow(10, digits - 1);

        int otpNum = min + rnd.nextInt(9 * min);

        String otp = String.valueOf(otpNum);

 

        String normalizedKey = key.trim().toLowerCase();

        store.put(normalizedKey, new Entry(otp, Instant.now().plusSeconds(ttlSeconds)));

        return otp;

    }

 

    /** Verify and consume OTP */

    public boolean verifyOtp(String key, String otp) {

        if (key == null || otp == null) return false;

        String normalizedKey = key.trim().toLowerCase();

        Entry e = store.get(normalizedKey);

        if (e == null) return false;

        if (Instant.now().isAfter(e.expiresAt)) { store.remove(normalizedKey); return false; }

        boolean ok = e.otp.equals(otp.trim());

        if (ok) store.remove(normalizedKey);

        return ok;

    }

 

    /** Debug helper */

    public String peekOtp(String key) {

        if (key == null) return null;

        Entry e = store.get(key.trim().toLowerCase());

        return e == null ? null : e.otp;

    }

}


