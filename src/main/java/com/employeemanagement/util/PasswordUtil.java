package com.employeemanagement.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Centralizes password hashing, verification, and hash-strength checks using BCrypt.
 */
public class PasswordUtil {

    // BCrypt work factor: increasing it raises the computational cost of hashing.
    private static final int LOG_ROUNDS = 12;
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Hash a plain-text password using BCrypt with production-grade log rounds.
     *
     * @param plainPassword the plain-text password to hash
     * @return the BCrypt hash string
     * @throws IllegalArgumentException if the password is null, empty, or too short
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }
        if (plainPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        String salt = BCrypt.gensalt(LOG_ROUNDS);
        return BCrypt.hashpw(plainPassword, salt);
    }

    /**
     * Verify a plain-text password against a BCrypt hash.
     * Uses constant-time comparison via BCrypt.checkpw to prevent timing attacks.
     *
     * @param plainPassword the plain-text password entered by the user
     * @param hashedPassword the stored BCrypt hash
     * @return true if the password matches the hash
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return false;
        }
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        // Ensure the stored hash looks like a valid BCrypt hash
        if (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2b$")) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format — treat as non-matching rather than throwing
            return false;
        }
    }

    /**
     * Check whether a stored password needs to be rehashed (e.g., old log rounds).
     *
     * @param hashedPassword the stored BCrypt hash
     * @return true if the hash should be rehashed with current settings
     */
    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        // Rehash if not a valid BCrypt hash or if log rounds are below current standard
        if (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2b$")) {
            return true;
        }
        try {
            String[] parts = hashedPassword.split("\\$");
            if (parts.length >= 3) {
                int storedRounds = Integer.parseInt(parts[2]);
                return storedRounds < LOG_ROUNDS;
            }
        } catch (NumberFormatException ignored) {
            return true;
        }
        return false;
    }
}
