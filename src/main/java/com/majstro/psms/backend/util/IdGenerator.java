package com.majstro.psms.backend.util;

import java.security.SecureRandom;


public class IdGenerator {

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

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
        
        
        id.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        
        
        for (int i = 0; i < 2; i++) {
            id.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        
        return id.toString();
    }
}
