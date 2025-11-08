package com.majstro.psms.backend.util;

import java.security.SecureRandom;

/**
 * Utility class to generate custom IDs with format: 2 uppercase letters + 2 digits
 * Example: AB12, XY99, PJ47
 */
public class IdGenerator {

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates a unique 4-character ID with 2 letters and 2 numbers
     * Format: [A-Z][A-Z][0-9][0-9]
     * 
     * @return A 4-character string ID (e.g., "AB12")
     */
    public static String generateId() {
        StringBuilder id = new StringBuilder(4);
        
        // Generate 2 random uppercase letters
        for (int i = 0; i < 2; i++) {
            id.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }
        
        // Generate 2 random digits
        for (int i = 0; i < 2; i++) {
            id.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        
        return id.toString();
    }

    /**
     * Generates a unique ID with a prefix for better identification
     * 
     * @param prefix Single character prefix (e.g., "P" for Project, "U" for User)
     * @return A 4-character string ID with prefix (e.g., "P" + "A12" = "PA12")
     */
    public static String generateIdWithPrefix(String prefix) {
        if (prefix == null || prefix.length() != 1) {
            throw new IllegalArgumentException("Prefix must be exactly 1 character");
        }
        
        StringBuilder id = new StringBuilder(4);
        id.append(prefix.toUpperCase());
        
        // Generate 1 more letter
        id.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        
        // Generate 2 digits
        for (int i = 0; i < 2; i++) {
            id.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        
        return id.toString();
    }
}
