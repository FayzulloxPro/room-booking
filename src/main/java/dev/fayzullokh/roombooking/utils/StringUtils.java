package dev.fayzullokh.roombooking.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class StringUtils {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();
    private static final Set<String> generatedStrings = new HashSet<>();

    public static String generateCode() {
        while (true) {
            int length = RANDOM.nextInt(4) + 5; // Generate a length between 5 and 8
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int index = RANDOM.nextInt(CHARACTERS.length());
                sb.append(CHARACTERS.charAt(index));
            }
            String newString = sb.toString();
            // Ensure uniqueness
            if (generatedStrings.add(newString)) {
                return newString;
            }
        }
    }
}
