package com.employeemanagement.util;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Centralizes password normalization, hashing, and verification.
 * Normal accounts use BCrypt. The single public demo account uses a plain-text
 * password by explicit project configuration and must never be used for real data.
 */
public class PasswordUtil {

    private static final int LOG_ROUNDS = 12;
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Deliberate exception for the publicly documented, low-privilege demo account only.
    private static final String DEMO_EMAIL = "demo.employee@example.com";
    private static final String DEMO_ROLE = "employee";

    /**
     * Removes whitespace from the beginning/end of a password and rejects whitespace
     * anywhere inside it. Unicode whitespace, non-breaking spaces and zero-width
     * space characters are treated as whitespace.
     */
    public static String normalizePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null.");
        }

        int start = 0;
        int end = password.length();

        while (start < end) {
            int codePoint = password.codePointAt(start);
            if (!isWhitespace(codePoint)) {
                break;
            }
            start += Character.charCount(codePoint);
        }

        while (end > start) {
            int codePoint = password.codePointBefore(end);
            if (!isWhitespace(codePoint)) {
                break;
            }
            end -= Character.charCount(codePoint);
        }

        String normalized = password.substring(start, end);

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty.");
        }

        if (normalized.codePoints().anyMatch(PasswordUtil::isWhitespace)) {
            throw new IllegalArgumentException(
                    "Password cannot contain spaces or whitespace characters."
            );
        }

        if (normalized.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters."
            );
        }

        return normalized;
    }

    private static boolean isWhitespace(int codePoint) {
        return Character.isWhitespace(codePoint)
                || Character.isSpaceChar(codePoint)
                || codePoint == 0x200B  // zero-width space
                || codePoint == 0xFEFF; // zero-width no-break space / BOM
    }

    /**
     * Hash a normalized password using BCrypt with 12 log rounds.
     */
    public static String hash(String plainPassword) {
        String normalized = normalizePassword(plainPassword);
        String salt = BCrypt.gensalt(LOG_ROUNDS);
        return BCrypt.hashpw(normalized, salt);
    }

    /**
     * Store the demo account password as plain text; hash every other account with BCrypt.
     * Only the exact published demo email and employee role receive this exception.
     */
    public static String hashForAccount(String email, String role, String plainPassword) {
        String normalized = normalizePassword(plainPassword);
        return isDemoAccount(email, role) ? normalized : hash(normalized);
    }

    /**
     * Verify a demo password by constant-time comparison. All other accounts
     * continue to require a valid BCrypt hash.
     */
    public static boolean verifyForAccount(
            String email,
            String role,
            String plainPassword,
            String storedPassword
    ) {
        if (storedPassword == null) {
            return false;
        }

        final String normalized;
        try {
            normalized = normalizePassword(plainPassword);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        if (isDemoAccount(email, role)) {
            return MessageDigest.isEqual(
                    normalized.getBytes(StandardCharsets.UTF_8),
                    storedPassword.getBytes(StandardCharsets.UTF_8)
            );
        }

        return verify(normalized, storedPassword);
    }

    /** The demo account is intentionally excluded from BCrypt hash upgrades. */
    public static boolean needsRehashForAccount(String email, String role, String storedPassword) {
        return !isDemoAccount(email, role) && needsRehash(storedPassword);
    }

    private static boolean isDemoAccount(String email, String role) {
        return email != null
                && role != null
                && DEMO_EMAIL.equalsIgnoreCase(email.trim())
                && DEMO_ROLE.equalsIgnoreCase(role.trim());
    }

    /** Verify a normalized password against a BCrypt hash. */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (hashedPassword == null
                || (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2b$"))) {
            return false;
        }

        final String normalized;
        try {
            normalized = normalizePassword(plainPassword);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        try {
            return BCrypt.checkpw(normalized, hashedPassword);
        } catch (IllegalArgumentException exception) {
            // Invalid hash format — treat as non-matching rather than throwing.
            return false;
        }
    }

    /** Check whether a stored BCrypt password needs to be rehashed. */
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
