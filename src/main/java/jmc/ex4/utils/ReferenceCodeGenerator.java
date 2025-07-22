package jmc.ex4.utils;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/**
 * Utility class to generate a random reference code.
 * The code is prefixed with "APT-" and consists of 8 alphanumeric characters.
 */
@Component
public class ReferenceCodeGenerator {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();


    /**
     * Generates a random reference code.
     *
     * @return A string representing the reference code.
     */
    public String generateReferenceCode() {
        StringBuilder code = new StringBuilder("APT-");
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
