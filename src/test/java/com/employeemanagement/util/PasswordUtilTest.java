package com.employeemanagement.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hash_returnsBcryptHash() {
        String hash = PasswordUtil.hash("secure123");
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertEquals(60, hash.length());
    }

    @Test
    void hash_throwsOnNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(null));
    }

    @Test
    void hash_throwsOnEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(""));
    }

    @Test
    void hash_throwsOnShortPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash("abc"));
    }

    @Test
    void hash_producesDifferentHashesEachTime() {
        String hash1 = PasswordUtil.hash("password1");
        String hash2 = PasswordUtil.hash("password1");
        assertNotEquals(hash1, hash2, "BCrypt salts should produce different hashes");
    }

    @Test
    void verify_returnsTrueForCorrectPassword() {
        String hash = PasswordUtil.hash("mypassword");
        assertTrue(PasswordUtil.verify("mypassword", hash));
    }

    @Test
    void verify_returnsFalseForWrongPassword() {
        String hash = PasswordUtil.hash("correct1");
        assertFalse(PasswordUtil.verify("wrong123", hash));
    }

    @Test
    void verify_returnsFalseForNullPassword() {
        assertFalse(PasswordUtil.verify(null, "$2a$12$somehash"));
    }

    @Test
    void verify_returnsFalseForEmptyPassword() {
        assertFalse(PasswordUtil.verify("", "$2a$12$somehash"));
    }

    @Test
    void verify_returnsFalseForNullHash() {
        assertFalse(PasswordUtil.verify("password", null));
    }

    @Test
    void verify_returnsFalseForEmptyHash() {
        assertFalse(PasswordUtil.verify("password", ""));
    }

    @Test
    void verify_returnsFalseForInvalidHashFormat() {
        assertFalse(PasswordUtil.verify("password", "not-a-hash"));
    }

    @Test
    void needsRehash_returnsTrueForLowRounds() {
        // $2a$04$... means 4 rounds, below the 12-round threshold
        assertTrue(PasswordUtil.needsRehash("$2a$04$somehashvalue"));
    }

    @Test
    void needsRehash_returnsFalseForCurrentRounds() {
        // $2a$12$... means 12 rounds, meets the threshold
        assertFalse(PasswordUtil.needsRehash("$2a$12$somehashvalue"));
    }

    @Test
    void needsRehash_returnsFalseForNullHash() {
        assertFalse(PasswordUtil.needsRehash(null));
    }

    @Test
    void needsRehash_returnsFalseForEmptyHash() {
        assertFalse(PasswordUtil.needsRehash(""));
    }

    @Test
    void needsRehash_returnsTrueForInvalidFormat() {
        assertTrue(PasswordUtil.needsRehash("invalid-format"));
    }

    @Test
    void hashAndVerify_roundTrip() {
        String plain = "TestPassword123!";
        String hash = PasswordUtil.hash(plain);
        assertTrue(PasswordUtil.verify(plain, hash));
        assertFalse(PasswordUtil.verify("WrongPassword", hash));
    }

    @Test
    void demoAccountUsesPlainTextOnlyForPublishedEmployeeIdentity() {
        String email = "demo.employee@example.com";
        String role = "employee";
        String password = "DemoEmployee@2026!";

        assertEquals(password, PasswordUtil.hashForAccount(email, role, password));
        assertTrue(PasswordUtil.verifyForAccount(email, role, password, password));
        assertFalse(PasswordUtil.verifyForAccount(email, role, "wrong-password", password));
        assertFalse(PasswordUtil.needsRehashForAccount(email, role, password));
    }

    @Test
    void nonDemoAccountsContinueToUseBcrypt() {
        String password = "secure123";
        String stored = PasswordUtil.hashForAccount("person@example.com", "employee", password);

        assertTrue(stored.startsWith("$2a$") || stored.startsWith("$2b$"));
        assertTrue(PasswordUtil.verifyForAccount("person@example.com", "employee", password, stored));
        assertFalse(PasswordUtil.verifyForAccount("person@example.com", "employee", "wrong-pass", stored));
    }

    @Test
    void demoEmailWithAdminRoleStillUsesBcrypt() {
        String password = "secure123";
        String stored = PasswordUtil.hashForAccount("demo.employee@example.com", "admin", password);

        assertTrue(stored.startsWith("$2a$") || stored.startsWith("$2b$"));
        assertTrue(PasswordUtil.verifyForAccount("demo.employee@example.com", "admin", password, stored));
    }


    @Test
    void normalizePassword_trimsUnicodeWhitespaceOnlyAtTheEdges() {
        assertEquals(
                "DemoEmployee@2026!",
                PasswordUtil.normalizePassword(" \\tDemoEmployee@2026!\\u00A0")
        );
    }

    @Test
    void normalizePassword_rejectsWhitespaceInsidePassword() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordUtil.normalizePassword("Demo Employee@2026!"));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordUtil.normalizePassword("Demo\\tEmployee@2026!"));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordUtil.normalizePassword("Demo\\nEmployee@2026!"));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordUtil.normalizePassword("Demo\\u00A0Employee@2026!"));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordUtil.normalizePassword("Demo\\u200BEmployee@2026!"));
    }

    @Test
    void demoAccountVerificationAcceptsTrimmedInputButRejectsInternalWhitespace() {
        String email = "demo.employee@example.com";
        String storedPassword = "DemoEmployee@2026!";

        assertTrue(PasswordUtil.verifyForAccount(
                email, "employee", "  DemoEmployee@2026!\\t", storedPassword
        ));
        assertFalse(PasswordUtil.verifyForAccount(
                email, "employee", "Demo Employee@2026!", storedPassword
        ));
    }

    @Test
    void hashForAccountStoresNormalizedPassword() {
        assertEquals(
                "DemoEmployee@2026!",
                PasswordUtil.hashForAccount(
                        "demo.employee@example.com",
                        "employee",
                        "  DemoEmployee@2026!  "
                )
        );
    }

    @Test
    void hashAndBcryptVerificationUseTheSamePasswordNormalization() {
        String hash = PasswordUtil.hash("  secure123  ");
        assertTrue(PasswordUtil.verify("secure123", hash));
        assertFalse(PasswordUtil.verify("secure 123", hash));
    }

}
