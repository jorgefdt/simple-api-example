package com.viafoura.examples.simpleapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides services related to palindrome strings.
 */
public class ExampleService {
    private static final Logger logger = LogManager.getLogger(ExampleService.class);
    private final List<String> palindromeKeys = new ArrayList<>();

    public List<String> getPalindromeKeys() {
        return palindromeKeys;
    }

    public Integer getPalindromeKeysSize() {
//        AppConfig.vfmClient.meter("dataBurritoGetPalindromeKeysSize").mark();
        return getPalindromeKeys().size();
    }

    public void collectPalindromeKeys(String fileName) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Can't use an empty file name.");
        } else {
            collectPalindromeKeys(new File(fileName));
        }
    }

    public void collectPalindromeKeys(File file) throws IOException {
        logger.info("Collecting simpleApiExample from file '{}'.", file);
        if (!file.exists()) {
            throw new IOException("Can't find file " + file);
        } else {
            Files.lines(file.toPath())
                    .map(ExampleService::parseJsonAndGetKeyOrNull)
                    .filter(this::isPalindrome)
                    .forEach(this.palindromeKeys::add);
            logger.info("Collected {} simpleApiExample.", this.palindromeKeys.size());
        }
    }

    public boolean isPalindrome(final String text) {
        boolean isPalindrome = true;
        if (text == null || text.isEmpty()) {
            isPalindrome = false;
        } else {
            final int length = text.length();
            final int halfLength = length / 2;
            for (int i = 0; i < halfLength; i++) {
                if (text.charAt(i) != text.charAt(length - i - 1)) {
                    isPalindrome = false;
                    break;
                }
            }
        }

        return isPalindrome;
    }


    private static String parseJsonAndGetKeyOrNull(final String json) {
        String key = null;
        try {
            final JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(json);
            key = (String) jsonObject.get("key");
        } catch (ParseException e) {
            logger.warn("Could not parse line '" + json + "': " + e.getMessage());
        }

        return key;
    }
}
