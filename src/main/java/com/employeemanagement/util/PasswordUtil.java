package com.employeemanagement.util;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Centralizes password hashing and verification.
 * Normal accounts use BCrypt. The single public demo account uses a plain-text
 * password by explicit project configuration and must never be used for real data.
 */
public class PasswordUtil {

    // BCrypt work factor: increasing it raises the computational cost of hashing.
    private static final int LOG_ROUNDS = 12;
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Deliberate exception for the publicly documented, low-privilege demo account only.
    private static final String DEMO_EMAIL = "demo.employee@example.com";
    private static final String DEMO_ROLE = "employee";

    /**
     * Hash a plain-text password using BCrypt with production-grade log rounds.
     */
    public static String hash(String plainPassword) {
        validatePassword(plainPassword);
        String salt = BCrypt.gensalt(LOG_ROUNDS);
        return BCrypt.hashpw(plainPassword, salt);
    }

    /**
     * Store the demo account password as plain text; hash every other account with BCrypt.
     * The demo exception is limited to its exact public email and employee role.
     */
    public static String hashForAccount(String email, String role, String plainPassword) {
        validatePassword(plainPassword);
        return isDemoAccount(email, role) ? plainPassword : hash(plainPassword);
    }

    /**
     * Verify the public demo password by direct comparison. All other accounts
     * continue to require a valid BCrypt hash.
     */
    public static boolean verifyForAccount(
            String email,
            String role,
            String plainPassword,
            String storedPassword
    ) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }

        if (isDemoAccount(email, role)) {
            return MessageDigest.isEqual(
                    plainPassword.getBytes(StandardCharsets.UTF_8),
                    storedPassword.getBytes(StandardCharsets.UTF_8)
            );
        }

        return verify(plainPassword, storedPassword);
    }

    /**
     * The demo account is intentionally excluded from BCrypt hash upgrades.
     */
    public static boolean needsRehashForAccount(String email, String role, String storedPassword) {
        return !isDemoAccount(email, role) && needsRehash(storedPassword);
    }

    private static boolean isDemoAccount(String email, String role) {
        return email != null
                && role != null
                && DEMO_EMAIL.equalsIgnoreCase(email.trim())
                && DEMO_ROLE.equalsIgnoreCase(role.trim());
    }

    private static void validatePassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }
        if (plainPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters"
            );
        }
    }

    /**
     * Verify a plain-text password against a BCrypt hash.
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return false;
        }
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        if (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2b$")) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format — treat as non-matching rather than throwing.
            return false;
        }
    }

    /**
     * Check whether a stored BCrypt password needs to be rehashed.
     */
    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
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
