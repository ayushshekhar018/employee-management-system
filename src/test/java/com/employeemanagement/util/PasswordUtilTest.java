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
}
