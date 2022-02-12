package com.idan.teamusup.services;

import java.util.Date;
import java.util.Random;

public class Generator {

    private static Generator instance;

    public static void init() {
        if (instance == null) {
            instance = new Generator();
        }
    }

    public static Generator getInstance() {
        return instance;
    }

    public String generateRandomString() {
        final int leftLimit = 48; // numeral '0'
        final int rightLimit = 122; // letter 'z'
        final int targetStringLength = 10;
        Random random = new Random();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return random.ints(leftLimit, rightLimit + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        } else {
            return "temp" + new Date().getTime();
        }
    }

    public String createKey(String id, String str) {
        final String DELIMITER = "&&";
        return (id + DELIMITER + str);
    }
}
